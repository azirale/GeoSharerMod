﻿namespace net.azirale.civcraft.GeoSharer
{
    public struct GeoBlockData
    {
        public readonly byte ID;
        public readonly byte Meta;

        public GeoBlockData(byte id, byte meta)
        {
            ID = id;
            Meta = meta;
        }
    }
}
