﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace net.azirale.civcraft.GeoSharer
{
    class Program
    {
        static void Main(string[] args)
        {
            InteractiveConsole interactive = new InteractiveConsole();
            interactive.Activate();
            Console.WriteLine("Program ended. Press [Enter] to exit.");
            Console.ReadLine();
        }
    }

    class InteractiveConsole
    {
        bool quitting = false;
        public void Activate()
        {
            
            while (!quitting)
            {
                ReadCommand();
            }
            Console.WriteLine("Leaving interactive mode");
        }

        private void ReadCommand()
        {
            Console.Write("> ");
            string input = Console.ReadLine();
            string inputLower = input.ToLower();

            switch (inputLower)
            {
                case "test": Command_Test(); return;
                case "quit": quitting = true; return;
                default: Console.WriteLine("Unknown command. Only commands active are 'test' and 'quit'"); return;
            }
            // you'll never get here
        }

        private void Command_Test()
        {
            Console.WriteLine(@"Reading file [C:\CustomPrograms\Minecraft\forge\mcp\jars\mods\GeoSharer\20130730_234728.geosharer");
            string path = @"C:\CustomPrograms\Minecraft\forge\mcp\jars\mods\GeoSharer\20130730_234728.geosharer";
            Reader reader = new Reader();
            reader.ReadFile(path);
            Console.WriteLine("Done reading file.\nReport:");
            Console.Write(reader.Report);
            GeoWorldBuilder builder = new GeoWorldBuilder();
            builder.CreateWorld(@"C:\CustomPrograms\Minecraft\forge\mcp\jars\mods\GeoSharer", reader.GeoChunks);
        }
    }
}