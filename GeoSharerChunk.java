package net.azirale.geosharer.mod;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.zip.GZIPOutputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

class GeoSharerChunk
{
	public int x;
	public int z;
	public int dimension;
	public long timestamp;
	public byte bytes[];
	
	
	// Private constructor to force factory method
	private GeoSharerChunk() { }
		
	// Equality methods	
	public boolean equals(Object other)
	{
		if (other instanceof GeoSharerChunk) return Equals((GeoSharerChunk)other);
		return false;
	}
	
	private boolean Equals(GeoSharerChunk other)
	{
		return
				this.x==other.x && 
				this.z==other.z && 
				this.dimension==other.dimension;
	}
	
	private boolean Equals(Chunk other)
	{
		return
				this.x == other.xPosition &&
				this.z==other.zPosition &&
				this.dimension==other.getWorld().provider.getDimensionId();
	}
		

	public static GeoSharerChunk CreateFromChunk(Chunk chunk) {
		GeoSharerChunk value = new GeoSharerChunk();
		if (chunk == null)
		{
			System.err.println("GeoSharer: Was passed a null chunk for GeoSharerChunk.CreateFromChunk()");
			value.bytes = null;
			return value;
		}
		// Set this object's ID
		value.x = chunk.xPosition;
		value.z = chunk.zPosition;
		value.dimension = chunk.getWorld().provider.getDimensionId();
		value.timestamp = System.currentTimeMillis();
		NBTTagCompound root = getChunkNBT(chunk, value.timestamp);
		value.bytes = bytesFromNBT(root);
		return value;
	}
	
	private static NBTTagCompound getChunkNBT(Chunk chunk, long timestamp)
    {
		NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound level = new NBTTagCompound();
        root.setTag("Level", level);
        level.setInteger("xPos", chunk.xPosition);
        level.setInteger("zPos", chunk.zPosition);
        NBTTagList sections = new NBTTagList();

        ExtendedBlockStorage[] allBlocks = chunk.getBlockStorageArray();
        int i = allBlocks.length;
        NBTTagCompound thisSection;
        for (int j = 0; j < i; ++j)
        {
            ExtendedBlockStorage blocks = allBlocks[j];

            // ***************************************************************************
            // based on net.minecraft.world.storage.chunk.AnvilChunkLoader.writeChunkToNBT
            // to ensure the NBT structure matches minecraft's output
            if (blocks != null)
            {
            	thisSection = new NBTTagCompound();
            	thisSection.setByte("Y", (byte)(blocks.getYLocation() >> 4 & 255));
                byte[] abyte = new byte[blocks.getData().length];
                NibbleArray nibblearray = new NibbleArray();
                NibbleArray nibblearray1 = null;
                for (int k = 0; k < blocks.getData().length; ++k)
                {
                    char c0 = blocks.getData()[k];
                    int l = k & 15;
                    int i1 = k >> 8 & 15;
                    int j1 = k >> 4 & 15;

                    if (c0 >> 12 != 0)
                    {
                        if (nibblearray1 == null)
                        {
                            nibblearray1 = new NibbleArray();
                        }

                        nibblearray1.set(l, i1, j1, c0 >> 12);
                    }

                    abyte[k] = (byte)(c0 >> 4 & 255);
                    nibblearray.set(l, i1, j1, c0 & 15);
                }

                thisSection.setByteArray("Blocks", abyte);
                thisSection.setByteArray("Data", nibblearray.getData());

                if (nibblearray1 != null)
                {
                	thisSection.setByteArray("Add", nibblearray1.getData());
                }
                /* No block light stored in our data ********************************************
                thisSection.setByteArray("BlockLight", blocks.getBlocklightArray().getData());
                */
                /* No sky light stored in our data **********************************************
                if (flag)
                {
                	thisSection.setByteArray("SkyLight", blocks.getSkylightArray().getData());
                }
                else
                {
                	thisSection.setByteArray("SkyLight", new byte[blocks.getBlocklightArray().getData().length]);
                }
                */
                sections.appendTag(thisSection);
            }
            // ***************************************************************************
        }
        level.setTag("Sections", sections);
        level.setByteArray("Biomes", chunk.getBiomeArray());
        return root;
    }
	
	// reflection to access the write method of NBTTagCompound
	private static Method NBTTagCompoundWrite = null;
	private static void AcquireNBTTagCompoundWrite()
	{
		for (Method method : NBTTagCompound.class.getDeclaredMethods())
		{
			if (!method.getReturnType().equals(void.class)) continue;
			Class[] x = method.getParameterTypes();
			if (x.length==1 && x[0].equals(DataOutput.class))
			{
				method.setAccessible(true);
				NBTTagCompoundWrite=method;
				break;
			}
		}
		if (NBTTagCompoundWrite==null) System.err.println("GeoSharer: Did not get NBTTagCompound write Method");
	}
	
	private static byte[] bytesFromNBT(NBTTagCompound root)
	{
		try
		{
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			DataOutputStream dataOut = new DataOutputStream(new GZIPOutputStream(byteStream));
			
			// root.write(dataOut);
			// method is not public in 1.8, so reflection to the rescue!
			if (NBTTagCompoundWrite==null) AcquireNBTTagCompoundWrite();
			try { NBTTagCompoundWrite.invoke(root, dataOut); }
			catch (Exception e) { System.err.println("GeoSharer: Reflecting access to NBTTagCompound.write failed miserably"); }
			
			dataOut.close();
			byte[] value = byteStream.toByteArray();
			return value;
		} catch (IOException e) {
			System.err.println("GeoSharer: Hit an exception when trying to encode new GeoChunkNBT byte array");
			return null;
		}
	}
}
