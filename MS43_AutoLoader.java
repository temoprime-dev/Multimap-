//====================================================
// MS43 AutoLoader v11
// MULTIMAP SAFE ZONE SCANNER
//
// Target:
// Siemens MS43
//
// Purpose:
// Find clean memory area for Multimap storage
//
// File name MUST be:
// MS43_AutoLoader.java
//====================================================

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;


public class MS43_AutoLoader extends GhidraScript {


    // Multimap candidate zone
    static final long ZONE_START = 0x04A8CB;
    static final long ZONE_END   = 0x050202;


    static final int MIN_FREE = 64;


    ArrayList<Long> references = new ArrayList<>();


    @Override
    public void run() throws Exception {


        println("==========================");
        println(" MS43 AutoLoader v11");
        println(" MULTIMAP SAFE ZONE SCANNER");
        println("==========================");


        println(String.format(
            "ZONE START : %06X",
            ZONE_START
        ));

        println(String.format(
            "ZONE END   : %06X",
            ZONE_END
        ));


        scanReferences();

        scanFree();


        println("==========================");
        println(" MS43 ENGINE COMPLETE");
        println("==========================");

    }



    //-------------------------------------------------
    // Search references into zone
    //-------------------------------------------------

    void scanReferences()
    {

        println("");
        println("[+] Checking references");


        for(long addr=0; addr<currentProgram.getMemory()
                .getMaxAddress().getOffset(); addr+=2)
        {

            try
            {

                Address a =
                toAddr(addr);


                int value =
                getShort(a) & 0xffff;


                if(value >= ZONE_START &&
                   value <= ZONE_END)
                {

                    references.add(addr);


                    println(String.format(
                    "[REF] %06X -> %06X",
                    addr,
                    value));

                }


            }
            catch(Exception e)
            {

            }

        }


        println("");
        println(
        "TOTAL REFERENCES: "
        + references.size());

    }



    //-------------------------------------------------
    // Scan empty bytes
    //-------------------------------------------------

    void scanFree()
    {

        println("");
        println("[+] Searching FREE blocks");


        long start=-1;
        int size=0;


        for(long x=ZONE_START;
            x<=ZONE_END;
            x++)
        {

            try
            {

                int b =
                getByte(toAddr(x))
                & 0xff;


                if(b==0x00 ||
                   b==0xFF)
                {

                    if(start==-1)
                        start=x;


                    size++;

                }

                else
                {

                    reportBlock(start,size);

                    start=-1;
                    size=0;

                }


            }
            catch(Exception e)
            {

            }


        }


        reportBlock(start,size);


    }



    //-------------------------------------------------

    void reportBlock(long start,int size)
    {

        if(start==-1)
            return;


        if(size < MIN_FREE)
            return;


        long end=start+size-1;


        println("--------------------------");

        println(String.format(
        "FREE START : %06X",
        start));


        println(String.format(
        "FREE END   : %06X",
        end));


        println(
        "SIZE       : "
        + size
        +" bytes");


        checkReference(start,end);


    }



    //-------------------------------------------------

    void checkReference(long s,long e)
    {

        boolean safe=true;


        for(Long r:references)
        {

            if(r>=s && r<=e)
            {

                safe=false;

            }

        }


        if(safe)
        {

            println(
            "STATUS : SAFE MULTIMAP AREA");

        }
        else
        {

            println(
            "STATUS : HAS REFERENCES");

        }

    }


}