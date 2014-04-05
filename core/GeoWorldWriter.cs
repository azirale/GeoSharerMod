﻿using System;
using System.Collections.Generic;
using System.IO;
using Substrate;
using Substrate.Nbt;

namespace net.azirale.geosharer.core
{
    public class GeoWorldWriter : IMessageSender, IProgressSender
    {
        public void UpdateWorld(string worldPath, List<GeoChunkRaw> chunks)
        {
            if (!this.CreateOrLoadWorld(worldPath)) return;
            AnvilWorld world = AnvilWorld.Create(worldPath);
            SetWorldDefaults(world);
            // Break chunks into regions for better disk access
            List<GeoRegion> regions = this.GetRegionBreakout(chunks);
            RegionChunkManager rcm = world.GetChunkManager();
            foreach (GeoRegion region in regions)
            {
                foreach (GeoChunkRaw chunk in region.Chunks)
                {
                    if (rcm.ChunkExists(chunk.X, chunk.Z))
                    {
                        // check the timestamp, if it has one
                        AnvilChunk c = rcm.GetChunk(chunk.X,chunk.Z) as AnvilChunk;
                        TagNodeCompound level = c.Tree.Root["Level"] as TagNodeCompound;
                        if (level.ContainsKey("GeoTimestamp"))
                        {
                            long existingTimestamp = level["GeoTimestamp"].ToTagLong().Data;
                            if (existingTimestamp >= chunk.TimeStamp) continue;
                        }
                    }
                    ChunkRef cr = rcm.SetChunk(chunk.X, chunk.Z, chunk.GetAnvilChunk());
                    AlphaBlockCollection blocks = cr.Blocks;
                    blocks.RebuildHeightMap();
                    blocks.RebuildFluid();
                    blocks.RebuildBlockLight();
                    blocks.RebuildSkyLight();
                    blocks.StitchBlockLight();
                    blocks.StitchSkyLight();
                }
                rcm.Save(); // save to disk, we are done with this region
            }
            // all regions done
            world.Save();
        }

        private void SetWorldDefaults(AnvilWorld world)
        {
            Level level = world.Level;
            level.AllowCommands = true; // Allow cheats
            level.GameRules.CommandBlockOutput = false; // No command blocks... ?
            level.GameRules.DoFireTick = false; // Not fire spread
            level.GameRules.DoMobSpawning = false; // no mobs
            level.GameRules.MobGriefing = false; // no creeper damage to environment
            level.GameType = GameType.CREATIVE; // creative mode
            level.LastPlayed = (long)(DateTime.UtcNow - new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc)).TotalMilliseconds; // update the last played time, java style
            level.GeneratorName = "Superflat"; // superflat for generated chunks if loaded in MC
        }

        private List<GeoRegion> GetRegionBreakout(List<GeoChunkRaw> chunks)
        {
            List<GeoRegion> regions = new List<GeoRegion>();
            for (int i = chunks.Count-1; i >= 0; --i)
            {
                bool inserted = false;
                GeoChunkRaw chunk = chunks[i];
                foreach (GeoRegion region in regions)
                {
                    if (chunk.RX == region.X && chunk.RZ == region.Z)
                    {
                        region.Chunks.Add(chunk);
                        inserted = true;
                        continue;
                    }
                }
                if (!inserted)
                {
                    GeoRegion region = new GeoRegion(chunk.RX, chunk.RZ);
                    region.Chunks.Add(chunk);
                    regions.Add(region);
                }
            }
            return regions;
        }

        private bool CreateOrLoadWorld(string worldPath)
        {
            // Try to find the folder first, if it doesn't exist try and create it, if that fails then end with an error message
            if (!Directory.Exists(worldPath))
            {
                this.SendMessage(MessageVerbosity.Normal, "WorldBuilder.UpdateWorld(): Could not find directory, attempting to create");
                try
                {
                    Directory.CreateDirectory(worldPath);
                    this.SendMessage(MessageVerbosity.Normal, "WorldBuilder.UpdateWorld(): Created new world directory");
                }
                catch
                {
                    this.SendMessage(MessageVerbosity.Error, "WorldBuilder.UpdateWorld(): Unable to create directory, aborting method");
                    return false;
                }
            }
            return true;
        }




         /***** IMESSAGESENDER IMPLEMENTATION ****************************************************/
        #region IMessageSender Implementation
        /// <summary>
        /// Subscribe to this event to receive text messages from the world builder object. 
        /// </summary>
        public event Message Messaging;

        /// <summary>
        /// Internal method of the WorldBuilder to proc a Messaging event
        /// </summary>
        /// <param name="verbosity">Which verbosity channel the message should be sent on</param>
        /// <param name="text">The text of the message</param>
        private void SendMessage(MessageVerbosity verbosity, string text)
        {
            Message msg = this.Messaging;
            if (msg != null) msg(this, new MessagePacket(verbosity, text));
        }

        /// <summary>
        /// Returns the list of subscribers to the messaging event of this WorldBuilder object
        /// </summary>
        /// <returns>Subscribers to Messaging event</returns>
        public Message GetMessagingList()
        {
            return this.Messaging;
        }
        #endregion

        /***** IPROGRESSSENDER IMPLEMENTATION ***************************************************/
        #region IProgressSender Implementation
        /// <summary>
        /// Subscribe to this event to receive progress updates from the world builder object
        /// </summary>
        public event Progress Progressing;

        /// <summary>
        /// Internal method of the WorldBuilder to proc a Progressing event
        /// </summary>
        /// <param name="current">Current progress</param>
        /// <param name="maximum">Maximum progress</param>
        /// <param name="text">Accompanying text for this progress</param>
        private void SendProgress(long current, long maximum, string text)
        {
            Progress prg = this.Progressing;
            if (prg != null) prg(this, new ProgressPacket(current, maximum, text));
        }

        #endregion
    }
}
