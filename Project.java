import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import java.io.PrintStream;
import static java.lang.String.format;
import static java.lang.System.out;

public class Project {

                                            //#############  PRINT CLASS FOR THE TABLES #############//

  
    public static final class PrettyPrinter {

        private static final char BORDER_KNOT = '+';
        private static final char HORIZONTAL_BORDER = '-';
        private static final char VERTICAL_BORDER = '|';

        private static final String DEFAULT_AS_NULL = "(NULL)";

        private final PrintStream out;
        private final String asNull;
        
        public PrettyPrinter(PrintStream out) {
            this(out, DEFAULT_AS_NULL);
        }

        public PrettyPrinter(PrintStream out, String asNull) {
            if ( out == null ) {
                throw new IllegalArgumentException("No print stream provided");
            }
            if ( asNull == null ) {
                throw new IllegalArgumentException("No NULL-value placeholder provided");
            }
            this.out = out;
            this.asNull = asNull;
        }

        public void print(String[][] table) {
            if ( table == null ) {
                throw new IllegalArgumentException("No tabular data provided");
            }
            if ( table.length == 0 ) {
                return;
            }
            final int[] widths = new int[getMaxColumns(table)];
            adjustColumnWidths(table, widths);
            printPreparedTable(table, widths, getHorizontalBorder(widths));
        }

        private void printPreparedTable(String[][] table, int widths[], String horizontalBorder) {
            final int lineLength = horizontalBorder.length();
            out.println(horizontalBorder);
            for ( final String[] row : table ) {
                if ( row != null ) {
                    out.println(getRow(row, widths, lineLength));
                    out.println(horizontalBorder);
                }
            }
        }

        private String getRow(String[] row, int[] widths, int lineLength) {
            final StringBuilder builder = new StringBuilder(lineLength).append(VERTICAL_BORDER);
            final int maxWidths = widths.length;
            for ( int i = 0; i < maxWidths; i++ ) {
                builder.append(padRight(getCellValue(safeGet(row, i, null)), widths[i])).append(VERTICAL_BORDER);
            }
            return builder.toString();
        }

        private String getHorizontalBorder(int[] widths) {
            final StringBuilder builder = new StringBuilder(256);
            builder.append(BORDER_KNOT);
            for ( final int w : widths ) {
                for ( int i = 0; i < w; i++ ) {
                    builder.append(HORIZONTAL_BORDER);
                }
                builder.append(BORDER_KNOT);
            }
            return builder.toString();
        }

        private int getMaxColumns(String[][] rows) {
            int max = 0;
            for ( final String[] row : rows ) {
                if ( row != null && row.length > max ) {
                    max = row.length;
                }
            }
            return max;
        }

        private void adjustColumnWidths(String[][] rows, int[] widths) {
            for ( final String[] row : rows ) {
                if ( row != null ) {
                    for ( int c = 0; c < widths.length; c++ ) {
                        final String cv = getCellValue(safeGet(row, c, asNull));
                        final int l = cv.length();
                        if ( widths[c] < l ) {
                            widths[c] = l;
                        }
                    }
                }
            }
        }

        private String padRight(String s, int n) {
            return format("%1$-" + n + "s", s);
        }

        private  String safeGet(String[] array, int index, String defaultValue) {
            return index < array.length ? array[index] : defaultValue;
        }

        private String getCellValue(Object value) {
            return value == null ? "" : value.toString();
        }

    }



                                            //############# VARIABLES #############//

    //Random integers to fill the memory with (for testing)
    static Random rand = new Random();
  
    //This is a queue that contains the instructions read from "Instructions.txt" by the parser
    static Queue<String> Instructions = new LinkedList<>();
    
    //This is a queue that holds the content of the bus (Tag and result value)
    //It's a queue to handle the cases where more than one instruction finishes at the same time
    static Queue<String[]> Bus = new LinkedList<>();

    //This is the Instructions Queue that will be printed in the console
    static String[][] InstructionQueue;

    //This is the Register file holds 32 registers (extra row for table titles)
    static String[][] RegisterFile = new String[33][3];

    //The A column is ommitted in all stations/buffers

    //The A and M reservation stations, Fixed sizes: A has 3 slots, M has 2 slots (The extra row is for the table titles)
    //Table titles: Id, Time, Tag, Busy, op, Vj, Vk, Qj, Qk  
    static String[][] ReservationStation_A = new String[4][9];
    static String[][] ReservationStation_M = new String[3][9];

    //The Load and Store Buffers: Fixed sizes: 3 slots (extra row for table titles)
    //Table titles for Load Buffer: Id, Time, Tag, Busy, Address
    static String[][] LoadBuffer = new String[4][5];

    //Table titles for Store Buffer: Id, Time, Tag, Busy, Address, V, Q
    static String[][] StoreBuffer = new String[4][7];

    //Memory to fetch the data from. 512 locations total (from 0 to 511)
    static String[] Memory = new String[512];
    //Variable to keep track of the clock cycles
    static int clk = 0;

    //The latency (clk cycles for each type of instruction)
    static int AddLatency = 0;
    static int SubLatency = 0;
    static int MulLatency = 0;
    static int DivLatency = 0;
    static int LoadLatency = 0;
    static int StoreLatency = 0;


                                                //############# METHODS #############//



    //This is the parser method: It reads the "Instructions.txt" file and adds the instructions to the Instructions queue
    public static void parser(int AddClk,int SubClk, int MulClk, int DivClk, int LoadClk, int StoreClk){
        try {
        File f = new File("Instructions.txt");
        Scanner sc = new Scanner(f);
        while (sc.hasNextLine()) {
            String data = sc.nextLine();
            Instructions.add(data);
        }
        sc.close();

        AddLatency = AddClk;
        SubLatency = SubClk;
        MulLatency = MulClk;
        DivLatency = DivClk;
        LoadLatency = LoadClk;
        StoreLatency = StoreClk;

        //The 4 is the number of columns which are: Id, Instruction, Issue, Execute, Write result
        InstructionQueue = new String[Instructions.size() + 1][5];
        
        //Starts the system
        start();
        
        } catch (FileNotFoundException e) {
        System.out.println("An error occurred.");
        e.printStackTrace();
        }

    }

    public static void start(){

            //The initializeInstructionQueue method is called to add the instructions in the instruction queue to a 2D Array containing the instructions 
            //3ashan el printing ykoon shaklo a7san
            // for (int i = 0 ; i < Memory.length; i++)
            //     Memory[i] = String.valueOf(rand.nextInt(100));
            Arrays.fill(Memory,"0.0");
            Memory[20] = "666.66";
            initializeInstructionQueue();
            initializeRegisterFile();
            initializeBuffersAndReservationStations();
            printCycle();
            nextCycle();
        }

    //This method fills the 2D array for the register file
    private static void initializeRegisterFile(){
        RegisterFile[0][0] = "Register";
        RegisterFile[0][1] = "Qi";
        RegisterFile[0][2] = "Content";


        for(int i = 1; i < RegisterFile.length; i++ ){
            RegisterFile[i][0] = "F"+ (i-1);
            RegisterFile[i][1] = "0";
            RegisterFile[i][2] = String.valueOf(String.format("%.2f",0 + (101 - 0) * rand.nextDouble()));
        }

    }

    //This method fills the 2D array tables for the Buffers and the Reservation Stations 
    private static void initializeBuffersAndReservationStations() {
    
    ReservationStation_A[0][0] = ReservationStation_M[0][0] = LoadBuffer[0][0] = StoreBuffer[0][0] = "Id";
    ReservationStation_A[0][1] = ReservationStation_M[0][1] = LoadBuffer[0][1] = StoreBuffer[0][1] = "Time";
    ReservationStation_A[0][2] = ReservationStation_M[0][2] = LoadBuffer[0][2] = StoreBuffer[0][2] = "Tag";
    ReservationStation_A[0][3] = ReservationStation_M[0][3] = LoadBuffer[0][3] = StoreBuffer[0][3] = "Busy";
    ReservationStation_A[0][4] = ReservationStation_M[0][4] = "op";
    ReservationStation_A[0][5] = ReservationStation_M[0][5] = "Vj";
    ReservationStation_A[0][6] = ReservationStation_M[0][6] = "Vk";
    ReservationStation_A[0][7] = ReservationStation_M[0][7] = "Qj";
    ReservationStation_A[0][8] = ReservationStation_M[0][8] = "Qk";
    
    LoadBuffer[0][4] = StoreBuffer[0][4] = "Address";
    StoreBuffer[0][5] = "V";
    StoreBuffer[0][6] = "Q";

    for(int i = 1; i < ReservationStation_A.length; i++ ){
    ReservationStation_A[i][2] = "A"+ i;
    ReservationStation_A[i][3] = "0";
    }

    for(int i = 1; i < ReservationStation_M.length; i++ ){
    ReservationStation_M[i][2] = "M"+ i;
    ReservationStation_M[i][3] = "0";

    }

    for(int i = 1; i < LoadBuffer.length; i++ ){
    LoadBuffer[i][2] = "L" + i;
    StoreBuffer[i][2] = "S" + i;
    LoadBuffer[i][3] = "0";
    StoreBuffer[i][3] = "0";

    }

}

    //This method fills the 2D array table for the Instruction Queue 
    public static void initializeInstructionQueue() {
    //Initializing the titles for the instruction queue table
    
    InstructionQueue[0][0] = "Id";
    InstructionQueue[0][1] = "Instruction";
    InstructionQueue[0][2] = "Issue";
    InstructionQueue[0][3] = "Execute";
    InstructionQueue[0][4] = "Write Result";
    
    //Adding the instructions in their correct column
    for (int i = 1; i < InstructionQueue.length; i++){
        InstructionQueue[i][0] = String.valueOf(i);
        InstructionQueue[i][1] = Instructions.poll();
        
    }
    }

    //Method to print the output of each cycle
    public static void printCycle(){

        final PrettyPrinter printer = new PrettyPrinter(out);
        System.out.println("///////////////////////////////////////////////\n");
        System.out.println("Clock Cycle: " + clk);
        System.out.println("Bus: " + Arrays.deepToString(Bus.toArray()));
        printer.print(InstructionQueue);   
        System.out.println();
        printer.print(ReservationStation_A);
        System.out.println();
        printer.print(ReservationStation_M);
        System.out.println();
        printer.print(LoadBuffer);
        System.out.println();
        printer.print(StoreBuffer);    
        System.out.println();
        printer.print(RegisterFile);  
    }

    //Method to start the next cycle
    public static void nextCycle(){
        //Stop the program when all instructions have written their values
        while (!Done()){
            clk++;
            for(int i = 1; i < InstructionQueue.length; i++){

                //Separate the instruction to get the operation, registers, addresses, etc.
                String[] instruction = InstructionQueue[i][1].split(" ");
                //Get the instruction id
                String id = InstructionQueue[i][0];

                //If the instruction has been executed but not written the result (Could still be executing)
                if(InstructionQueue[i][3] != null && InstructionQueue[i][4] == null){

                    switch(instruction[0]){
                        case "L.D":
                            for(int j = 1; j < LoadBuffer.length; j++){
                                //If the instruction id matches
                                if(LoadBuffer[j][0] == id){
                                    //Continue executing the load instruction
                                    executeLoadInstruction(i, j);
                                }
                            }
                            break;
                            
                        case "S.D":
                            for(int j = 1; j < StoreBuffer.length; j++){
                                //If the instruction id matches
                                if(StoreBuffer[j][0] == id){
                                    //Continue executing the Store instruction
                                    executeStoreInstruction(i, j);
                                }         
                            }
                            break;

                        //Both cases are combined since the same thing will happen in both div and mul instructions;
                        case "MUL.D":
                        case "DIV.D":
                        for(int j = 1; j < ReservationStation_M.length; j++){
                            //If the instruction id matches
                            if(ReservationStation_M[j][0] == id){
                                //Continue executing the M instruction
                                executeMInstruction(i, j);
                            }         
                        }
                        break;
                        
                        //Both cases are combined since the same thing will happen in both add and sub instructions;
                        case "ADD.D":
                        case "SUB.D":
                        for(int j = 1; j < ReservationStation_A.length; j++){
                            //If the instruction id matches
                            if(ReservationStation_A[j][0] == id){
                                //Continue executing the A instruction
                                executeAInstruction(i, j);
                            }         
                        }
                        break; 
                    }

                }
                //If the instruction has been issued but not executed
                if(InstructionQueue[i][2] != null && InstructionQueue[i][3] == null){
                    //Find where the instruction is issued (in which station)
                    switch(instruction[0]){
                        case "L.D":
                            for(int j = 1; j < LoadBuffer.length; j++){
                                //If the instruction id matches
                                if(LoadBuffer[j][0] == id){
                                    //Execute the load instruction (Loads dont wait for any operands)
                                    executeLoadInstruction(i, j);
                                    //Write the time in the "Execute" column
                                    if(LoadLatency > 1)
                                        InstructionQueue[i][3] = clk + "...";
                                    else
                                        InstructionQueue[i][3] = String.valueOf(clk);
                                    
                                }
                            }
                            break;
                        case "S.D":
                            for(int j = 1; j < StoreBuffer.length; j++){
                                //If the instruction id matches
                                if(StoreBuffer[j][0] == id){
                                    //If the required value is available, Execute the instruction
                                    if(StoreBuffer[j][5] != null){
                                        executeStoreInstruction(i, j);
                                        //Write the time in the "Execute" column
                                        if(StoreLatency > 1)
                                            InstructionQueue[i][3] = clk + "...";
                                        else
                                            InstructionQueue[i][3] = String.valueOf(clk);

                                    }

                                    // else: (V is null so we are still waiting for the value in Q)
                                    else {
                                        //if the checkAvailable method doesn't return null, it means that the value is available
                                        if(checkAvailable(StoreBuffer[j][6], StoreBuffer[j][2]) != null){
                                            //If available, set the V value as the returned value and clear Q
                                            StoreBuffer[j][5] = checkAvailable(StoreBuffer[j][6], StoreBuffer[j][2]);
                                            StoreBuffer[j][6] = null;
                                        }
                                    }        
                                }
                            }
                            break;

                        case "MUL.D":
                            for(int j = 1; j < ReservationStation_M.length; j++){
                                //If the instruction id matches
                                if(ReservationStation_M[j][0] == id){
                                    //If both operands are available (Vj and Vk)
                                    if(ReservationStation_M[j][5] != null && ReservationStation_M[j][6] != null ){
                                        executeMInstruction(i, j);
                                            //Write the time in the "Execute" column
                                            if(MulLatency > 1)
                                            InstructionQueue[i][3] = clk + "...";
                                        else
                                            InstructionQueue[i][3] = String.valueOf(clk);
                                        
                                    }
                                    
                                    else{
                                        // Check if Qj is not empty (if there is a value we are still wating for)
                                        if(ReservationStation_M[j][7] != null){
                                            //If yes, check for it's availability
                                            //if the checkAvailable method doesn't return null, it means that the value is available
                                            if(checkAvailable(ReservationStation_M[j][7], ReservationStation_M[j][2]) != null){
                                                //If available, set the Vj value as the returned value and clear Qj
                                                ReservationStation_M[j][5] = checkAvailable(ReservationStation_M[j][7], ReservationStation_M[j][2]);
                                                ReservationStation_M[j][7] = null;
                                            }
                                        } 
                                        // Check if Qk is not empty (if there is a value we are still wating for)
                                        if(ReservationStation_M[j][8] != null){
                                            //If yes, check for it's availability
                                            //if the checkAvailable method doesn't return null, it means that the value is available
                                            if(checkAvailable(ReservationStation_M[j][8], ReservationStation_M[j][2]) != null){
                                                //If available, set the Vk value as the returned value and clear Qk
                                                ReservationStation_M[j][6] = checkAvailable(ReservationStation_M[j][8], ReservationStation_M[j][2]);
                                                ReservationStation_M[j][8] = null;
                                            }
                                        }               
                                    }
                                    
                                }
                            }
                                break;
                        case "DIV.D":
                        for(int j = 1; j < ReservationStation_M.length; j++){
                            //If the instruction id matches
                            if(ReservationStation_M[j][0] == id){
                                //If both operands are available (Vj and Vk)
                                if(ReservationStation_M[j][5] != null && ReservationStation_M[j][6] != null ){
                                    executeMInstruction(i, j);
                                        //Write the time in the "Execute" column
                                        if(DivLatency > 1)
                                            InstructionQueue[i][3] = clk + "...";
                                        else
                                            InstructionQueue[i][3] = String.valueOf(clk);
                                }
                                
                                else{
                                    // Check if Qj is not empty (if there is a value we are still wating for)
                                    if(ReservationStation_M[j][7] != null){
                                        //If yes, check for it's availability
                                        //if the checkAvailable method doesn't return null, it means that the value is available
                                        if(checkAvailable(ReservationStation_M[j][7], ReservationStation_M[j][2]) != null){
                                            //If available, set the Vj value as the returned value and clear Qj
                                            ReservationStation_M[j][5] = checkAvailable(ReservationStation_M[j][7], ReservationStation_M[j][2]);
                                            ReservationStation_M[j][7] = null;
                                        }
                                    } 
                                    // Check if Qk is not empty (if there is a value we are still wating for)
                                    if(ReservationStation_M[j][8] != null){
                                        //If yes, check for it's availability
                                        //if the checkAvailable method doesn't return null, it means that the value is available
                                        if(checkAvailable(ReservationStation_M[j][8], ReservationStation_M[j][2]) != null){
                                            //If available, set the Vk value as the returned value and clear Qk
                                            ReservationStation_M[j][6] = checkAvailable(ReservationStation_M[j][8], ReservationStation_M[j][2]);
                                            ReservationStation_M[j][8] = null;
                                        }
                                    }               
                                }
                                
                            }
                        }
                            break;
                        
                        //Both cases are combined since the same thing will happen in both add and sub instructions;
                        case "ADD.D":
                            for(int j = 1; j < ReservationStation_A.length; j++){
                                //If the instruction id matches
                                if(ReservationStation_A[j][0] == id){
                                    //If both operands are available (Vj and Vk)
                                    if(ReservationStation_A[j][5] != null && ReservationStation_A[j][6] != null ){

                                        executeAInstruction(i, j);
                                        if(AddLatency > 1)
                                        InstructionQueue[i][3] = clk + "...";
                                    else
                                        InstructionQueue[i][3] = String.valueOf(clk);
                                    }
                                    
                                    else{
                                        // Check if Qj is not empty (if there is a value we are still wating for)
                                        if(ReservationStation_A[j][7] != null){
                                            //If yes, check for it's availability
                                            //if the checkAvailable method doesn't return null, it means that the value is available
                                            if(checkAvailable(ReservationStation_A[j][7] ,ReservationStation_A[j][2]) != null){
                                                //If available, set the Vj value as the returned value and clear Qj
                                                ReservationStation_A[j][5] = checkAvailable(ReservationStation_A[j][7], ReservationStation_A[j][2]);
                                                ReservationStation_A[j][7] = null;
                                            }
                                           
                                        } 
                                        // Check if Qk is not empty (if there is a value we are still wating for)
                                        if(ReservationStation_A[j][8] != null){
                                            //If yes, check for it's availability
                                            //if the checkAvailable method doesn't return null, it means that the value is available
                                            if(checkAvailable(ReservationStation_A[j][8], ReservationStation_A[j][2]) != null){
                                                //If available, set the Vk value as the returned value and clear Qk
                                                ReservationStation_A[j][6] = checkAvailable(ReservationStation_A[j][8], ReservationStation_A[j][2]);
                                                ReservationStation_A[j][8] = null;
                                            }
                                        }               
                                    }
                                    
                                }
                            }
                            break; 
                        case "SUB.D":
                            for(int j = 1; j < ReservationStation_A.length; j++){
                                //If the instruction id matches
                                if(ReservationStation_A[j][0] == id){
                                    //If both operands are available (Vj and Vk)
                                    if(ReservationStation_A[j][5] != null && ReservationStation_A[j][6] != null ){

                                        executeAInstruction(i, j);
                                        if(SubLatency > 1)
                                            InstructionQueue[i][3] = clk + "...";
                                        else
                                            InstructionQueue[i][3] = String.valueOf(clk);

                                    }
                                    
                                    else{
                                        // Check if Qj is not empty (if there is a value we are still wating for)
                                        if(ReservationStation_A[j][7] != null){
                                            //If yes, check for it's availability
                                            //if the checkAvailable method doesn't return null, it means that the value is available
                                            if(checkAvailable(ReservationStation_A[j][7], ReservationStation_A[j][2]) != null){
                                                //If available, set the Vj value as the returned value and clear Qj
                                                ReservationStation_A[j][5] = checkAvailable(ReservationStation_A[j][7], ReservationStation_A[j][2]);
                                                ReservationStation_A[j][7] = null;
                                            }
                                        } 
                                        // Check if Qk is not empty (if there is a value we are still wating for)
                                        if(ReservationStation_A[j][8] != null){
                                            //If yes, check for it's availability
                                            //if the checkAvailable method doesn't return null, it means that the value is available
                                            if(checkAvailable(ReservationStation_A[j][8], ReservationStation_A[j][2]) != null){
                                                //If available, set the Vk value as the returned value and clear Qk
                                                ReservationStation_A[j][6] = checkAvailable(ReservationStation_A[j][8], ReservationStation_A[j][2]);
                                                ReservationStation_A[j][8] = null;
                                            }
                                        }               
                                    }
                                    
                                }
                            }
                            break; 
                    }
                                
                }

                //If the instruction hasn't been issued yet (value == null)
                else if(InstructionQueue[i][2] == null){                      
                    //See what type of operation it is
                        switch(instruction[0]){
                            case "L.D":
                                for(int j = 1; j < LoadBuffer.length; j++){
                                    //If there is an empty slot in the buffer (busy == 0)
                                    if(LoadBuffer[j][3] == "0"){
                                        //issue the instruction in it's matching station
                                        issueLoadInstruction(id, instruction, j);
                                        //Put the current clock cycle in the "issue" field of the Instruction queue
                                        InstructionQueue[i][2] = String.valueOf(clk);
                                        //Break out of loop because only one instruction will be issued to the station
                                        break;

                                    }
                                }
                                break;
                            case "S.D":
                            
                                for(int j = 1; j < StoreBuffer.length; j++){
                                    //If there is an empty slot in the buffer (busy == 0)
                                    if(StoreBuffer[j][3] == "0"){
                                        //issue the instruction in it's matching station
                                        issueStoreInstruction(id, instruction, j);
                                        //Put the current clock cycle in the "issue" field of the Instruction queue
                                        InstructionQueue[i][2] = String.valueOf(clk);
                                        //Break out of loop because only one instruction will be issued to the station
                                        break;

                                    }
                                }
                                break;

                            //Both cases are combined since the same thing will happen in both div and mul instructions;
                            case "MUL.D":
                            case "DIV.D":
                                for(int j = 1; j < ReservationStation_M.length; j++){
                                    //If there is an empty slot in the buffer (busy == 0)
                                    if(ReservationStation_M[j][3] == "0"){
                                        //issue the instruction in it's matching station
                                        issueStationM(id, instruction , j);
                                        //Put the current clock cycle in the "issue" field of the Instruction queue
                                        InstructionQueue[i][2] = String.valueOf(clk);
                                        //Break out of loop because only one instruction will be issued to the station
                                        break;

                                    }
                                }
                                break;

                            //Both cases are combined since the same thing will happen in both add and sub instructions;
                            case "ADD.D":
                            case "SUB.D":
                                for(int j = 1; j < ReservationStation_A.length; j++){
                                    //If there is an empty slot in the buffer (busy == 0)
                                    if(ReservationStation_A[j][3] == "0"){
                                        //issue the instruction in it's matching station
                                        issueStationA(id, instruction , j);
                                        //Put the current clock cycle in the "issue" field of the Instruction queue
                                        InstructionQueue[i][2] = String.valueOf(clk);
                                        //Break out of loop because only one instruction will be issued to the station
                                        break;
                                    }
                                }
                                break; 
                        }
                    //Break out of the main loop because only one instruction is issued during the clock cycle
                    break;
                }

            }

            // If the bus is not empty
            if(Bus.peek() != null){
                //There could be 3 values in case of store instructions (tag, address, value)
                //So we use the length of the result to see if we're going to update the memory (in case of a store) or not (every other instruction)
                if(Bus.peek().length == 3){
                    updateMemory(Bus.poll());
                }
                else
                    updateRegisterFile(Bus.poll());
            }
           
            printCycle();
            nextCycle();
        }
        
    }

    private static void executeAInstruction(int queueNumber, int location) {
          // int to keep track of the latency of the executing instruction
          int latency = 0;
          //If time left is not put under "Time"
          if(ReservationStation_A[location][1] == null){
              //Put the time of the load cycle in the "Time" column (Depends on whether the instruction is add or sub)
              if(ReservationStation_A[location][4].equals("ADD"))
                  latency = AddLatency;
              else
                  latency = SubLatency;
                  
              ReservationStation_A[location][1] = String.valueOf(latency);
      }
          else{
              //If time > 0, decrement
              if(Integer.parseInt(ReservationStation_A[location][1]) > 0){
                  ReservationStation_A[location][1] = String.valueOf(Integer.parseInt(ReservationStation_A[location][1]) - 1);
              }
              //If time = 0 (Execution is finished)
              if(ReservationStation_A[location][1].equals("0")){
                  String result = "";
                  double s1 = Double.parseDouble(ReservationStation_A[location][5]);
                  double s2 = Double.parseDouble(ReservationStation_A[location][6]);

                  switch(ReservationStation_A[location][4]){
                        case "ADD":
                            result = String.valueOf(String.format("%.4f",s1+s2));
                            latency = AddLatency;
                            break;
                        case "SUB":
                            result = String.valueOf(String.format("%.4f",s1-s2));
                            latency = SubLatency;

                  }
                  //Get the tag from the tag column
                  String tag = ReservationStation_A[location][2];
              
                  //Place the tag and result on the bus
                  Bus.add(new String[]{tag, result});

                  //Update time in "Execute" column if instruction takes > 1 clk cycle
                  //Update the "Write Result" column
                  updateExecutionTime(latency, queueNumber);
                  //Remove instruction from buffer
                  clearResA(location);
              }
          }

    }

    private static void executeMInstruction(int queueNumber, int location) {
            // int to keep track of the latency of the executing instruction
            int latency = 0;
            //If time left is not put under "Time"
            if(ReservationStation_M[location][1] == null){
                //Put the time of the load cycle in the "Time" column (Depends on whether the instruction is add or sub)
                if(ReservationStation_M[location][4].equals("MUL"))
                    latency = MulLatency;
                else
                    latency = DivLatency;
                    
                ReservationStation_M[location][1] = String.valueOf(latency);
        }
            else{
                //If time > 0, decrement
                if(Integer.parseInt(ReservationStation_M[location][1]) > 0){
                    ReservationStation_M[location][1] = String.valueOf(Integer.parseInt(ReservationStation_M[location][1]) - 1);
                }
                //If time = 0 (Execution is finished)
                if(ReservationStation_M[location][1].equals("0")){
                    String result = "";
                    double s1 = Double.parseDouble(ReservationStation_M[location][5]);
                    double s2 = Double.parseDouble(ReservationStation_M[location][6]);

                    switch(ReservationStation_M[location][4]){
                        case "MUL":
                            result = String.valueOf(String.format("%.4f",s1*s2));
                            latency = MulLatency;
                            break;
                        case "DIV":
                            result = String.valueOf(String.format("%.4f",s1/s2));
                            latency = DivLatency;

                    }
                    //Get the tag from the tag column
                    String tag = ReservationStation_M[location][2];
                
                    //Place the tag and result on the bus
                    Bus.add(new String[]{tag, result});

                    //Update time in "Execute" column if instruction takes > 1 clk cycle
                    //Update the "Write Result" column
                    updateExecutionTime(latency, queueNumber);
                    //Remove instruction from buffer

                    clearResM(location);
                }
            }
    }

    private static void executeStoreInstruction(int queueNumber, int location) {
            //If time left is not put under "Time"
            if(StoreBuffer[location][1] == null)
            //Put the time of the load cycle in the "Time" column
                StoreBuffer[location][1] = String.valueOf(StoreLatency);
            else{
                //If time > 0, decrement
                if(Integer.parseInt(StoreBuffer[location][1]) > 0){
                    StoreBuffer[location][1] = String.valueOf(Integer.parseInt(StoreBuffer[location][1]) - 1);
                }
                // If time is zero (finished executing):
                if(StoreBuffer[location][1].equals("0")){
                    //Get the tag from the tag column
                    String tag = StoreBuffer[location][2];
                    //Get the address from the address
                    String address = StoreBuffer[location][4];
                    //Get the value from V
                    String value = StoreBuffer[location][5];
                    //Place the tag, address and value on the bus
                    Bus.add(new String[]{tag, address, value});

                    //Update time in "Execute" column if instruction takes > 1 clk cycle
                    //Update the "Write Result" column
                    updateExecutionTime(StoreLatency, queueNumber);
                    //Remove instruction from buffer

                    clearBufferS(location);
                }
            }
    }

    private static void executeLoadInstruction(int queueNumber, int location) {
        
        //If time left is not put under "Time"
        if(LoadBuffer[location][1] == null)
            //Put the time of the load cycle in the "Time" column
            LoadBuffer[location][1] = String.valueOf(LoadLatency);
        else{
            //If time > 0, decrement
            //If time == 0, write result to bus
            if(Integer.parseInt(LoadBuffer[location][1]) > 0)
                LoadBuffer[location][1] = String.valueOf(Integer.parseInt(LoadBuffer[location][1]) - 1);

            //If time is Zero (finished executing):
            if(LoadBuffer[location][1].equals("0")){ 
            
                //Get the tag from the tag column
                String tag = LoadBuffer[location][2];
                //Get the data in the memory and convert it to string
                String value = Memory[Integer.parseInt(LoadBuffer[location][4])];
                //Place the tag and value on the bus
                Bus.add(new String[]{tag,value});
                
                //Update time in "Execute" column if instruction takes > 1 clk cycle
                //Update the "Write Result" column
                updateExecutionTime(LoadLatency, queueNumber);

                //Remove instruction from buffer
                clearBufferL(location);
                                             
            }
           
        }
      
    }

    private static void updateExecutionTime(int latency, int queueNumber) {
        if(latency > 1)
        InstructionQueue[queueNumber][3] += String.valueOf(clk - 1);
        //Update the value of the "Write Result" column with the current cycle
        InstructionQueue[queueNumber][4] = String.valueOf(clk+ (Bus.size()-1));
    }

    private static void clearResM(int location){
        for (int i = 0 ; i < ReservationStation_M[0].length; i++){
            if(i != 2){
                if (i == 3)
                    ReservationStation_M[location][3] = "0";
                else
                    ReservationStation_M[location][i] = null;
            }
        }
    }

    private static void clearResA(int location){
        for (int i = 0 ; i < ReservationStation_A[0].length; i++){
            if(i != 2){
                if (i == 3)
                    ReservationStation_A[location][3] = "0";
                else
                    ReservationStation_A[location][i] = null;
            }
        }
    }

    private static void clearBufferL(int location) {
        for (int i = 0 ; i < LoadBuffer[0].length; i++){
            if(i != 2){
                if (i == 3)
                    LoadBuffer[location][3] = "0";
                else
                    LoadBuffer[location][i] = null;
            }
        }
    }

    private static void clearBufferS(int location) {
        for (int i = 0 ; i < StoreBuffer[0].length; i++){
            if(i != 2){
                if (i == 3)
                    StoreBuffer[location][3] = "0";
                else
                    StoreBuffer[location][i] = null;
            }
        }
    }

    private static void issueStationA(String id, String[] instruction, int location) {
        //Set the instruction id 
        ReservationStation_A[location][0] = id;
        //Set the station to busy (busy = 1)
        ReservationStation_A[location][3] = "1";
        //Set the op field as the one in the instruction
        ReservationStation_A[location][4] = instruction[0].substring(0,3);
        //Getting the register number from the destination register (ex. the "6" in F6)
        String destinationRegister= instruction[1].substring(1);
      
        //Getting the two source registers
        String s1 = instruction[2].substring(1);
        String s2 = instruction[3].substring(1);
        
        
        //Check if first operand is available
        if(checkAvailable(s1, ReservationStation_A[location][2]) != null){
            
            //Set Vk as content of first register and clear Qk
            ReservationStation_A[location][5] = checkAvailable(s1, ReservationStation_A[location][2]);
            ReservationStation_A[location][7] = null;
        }
        else{
            //Set Qk as Tag of result operation and clear Vk
            ReservationStation_A[location][7] = RegisterFile[Integer.parseInt(s1) + 1][1];
            ReservationStation_A[location][5] = null;
        }

        if(checkAvailable(s2, ReservationStation_A[location][2]) != null){
            //Set Vj as content of first register and clear Qj
            ReservationStation_A[location][6] = checkAvailable(s2, ReservationStation_A[location][2]);
            ReservationStation_A[location][8] = null;
        }
        else{
            //Set Qj as Tag of result operation and clear Vj
            ReservationStation_A[location][8] = RegisterFile[Integer.parseInt(s2) + 1][1];
            ReservationStation_A[location][6] = null;
        }
        
          //Setting the value of the Destination register to the tag of this instruction
          RegisterFile[Integer.parseInt(destinationRegister)  + 1][1] = ReservationStation_A[location][2];


    }

    private static void issueStationM(String id, String[] instruction, int location) {
        //Set the instruction id 
        ReservationStation_M[location][0] = id;
        //Set the station to busy (busy = 1)
        ReservationStation_M[location][3] = "1";
        //Set the op field as the one in the instruction
        ReservationStation_M[location][4] = instruction[0].substring(0,3);
        //Getting the register number from the destination register (ex. the "6" in F6)
        String destinationRegister= instruction[1].substring(1);
     
        String s1 = instruction[2].substring(1);
        String s2 = instruction[3].substring(1);
        
        
        //Check if first operand is available
        if(checkAvailable(s1, ReservationStation_M[location][2]) != null){
            
            //Set Vk as content of first register and clear Qk
            ReservationStation_M[location][5] = checkAvailable(s1, ReservationStation_M[location][2]);
            ReservationStation_M[location][7] = null;
        }
        else{
            //Set Qk as Tag of result operation and clear Vk
            ReservationStation_M[location][7] = RegisterFile[Integer.parseInt(s1) + 1][1];
            ReservationStation_M[location][5] = null;
        }

        if(checkAvailable(s2, ReservationStation_M[location][2]) != null){
            //Set Vj as content of first register and clear Qj
            ReservationStation_M[location][6] = checkAvailable(s2, ReservationStation_M[location][2]);
            ReservationStation_M[location][8] = null;
        }
        else{
            //Set Qj as Tag of result operation and clear Vj
            ReservationStation_M[location][8] = RegisterFile[Integer.parseInt(s2) + 1][1];
            ReservationStation_M[location][6] = null;
        }
        
          //Setting the value of the Destination register to the tag of this instruction
          RegisterFile[Integer.parseInt(destinationRegister)  + 1][1] = ReservationStation_M[location][2];


    }

    private static void issueStoreInstruction(String id, String[] instruction, int location) {
        //Set the instruction id 
        StoreBuffer[location][0] = id;
        //Set the station to busy (busy = 1)
        StoreBuffer[location][3] = "1";
        //Set the Address field as  the one in the instruction
        StoreBuffer[location][4] = instruction[2];
        //Getting the register number from the source register (ex. the "6" in F6)
        String register = instruction[1].substring(1);

        if(checkAvailable(register, StoreBuffer[location][2]) != null){
            //Set V as content of register and clear Q
            StoreBuffer[location][5] = checkAvailable(register,StoreBuffer[location][2]);
            StoreBuffer[location][6] = null;
        }
        else{
            StoreBuffer[location][6] = RegisterFile[Integer.parseInt(register) + 1][1];
            StoreBuffer[location][5] = null;
        }
    }

    private static void issueLoadInstruction(String id, String[] instruction, int location) {
        //Set the instruction id 
        LoadBuffer[location][0] = id;
        //Set the station to busy (busy = 1)
        LoadBuffer[location][3] = "1";
        //Set the Address field as  the one in the instruction
        LoadBuffer[location][4] = instruction[2];
        //Getting the register number from the destination register (ex. the "6" in F6)
        String registerLocation = instruction[1].substring(1);
        //Setting the value of the Destination register to the tag of this instruction
        RegisterFile[Integer.parseInt(registerLocation)  + 1][1] = LoadBuffer[location][2];
    }

    private static String checkAvailable(String s, String tag) {
        //s (the input string) can either be a register location (if the instruction is being issued for the first time) or the tag of an instruction that should return the needed value (if the instruction has been issued but is still waiting for operands)
        //tag is the tag of the Instruction (for the case where the source register(s) is/are the same as the Destination Register)
        // like instruction with tag A1 being ADD F6 F6 F6 (in the reg file, F6 will be marked as A1, which would mean that we will wait for the instruction to finish first,
        // but in this case, A1 is the same instruction, so it will be stuck in an infinite loop (since it needs F6 to produce F6))
        try{
            //If s is for a register, it will be a value such as "0" (for F0) or "6" (for F6)
            //If it is for a tag, it will be "M1", "A1", etc. so it will not be able to get parsed as an integer (because "M1" is not a number, but "6" is)
            int registerLocation = Integer.parseInt(s);
            //If the value in the register file is clean (0) OR if the same instruction is producing the value for the register file (Ex. ADD F6 F6 F6)
            if(RegisterFile[registerLocation + 1][1] == "0" || RegisterFile[registerLocation + 1][1] == tag)
                return RegisterFile[registerLocation + 1][2];
            //If not in the register file, check the bus
            else if(Bus.peek() != null){
                if(Bus.peek()[0] == RegisterFile[registerLocation + 1][1])
                    return Bus.peek()[1];
        
            }
            return null;
        }
        //If we get an exception, it means that s was a tag and not a register location
        catch(Exception e) {
            //This means that we don't need to check the register file, but only monitor the Bus for the value we are waiting for
            if(Bus.peek() != null){
                //if the tag of the bus value is equal to the tag of the value we need
                if(Bus.peek()[0] == s)
                    // return the value we are waiting for
                    return Bus.peek()[1];   
            }
        }
        return null; 
    }

    private static void updateRegisterFile(String[] busData){
        // Loop over register file
        if(busData != null){
            for (int i = 1 ; i < RegisterFile.length; i ++ ){
                //If tag is found in the register file:
                if (busData[0].equals(RegisterFile[i][1])){
                    // Clear tag to be "0"
                    RegisterFile[i][1] = "0";
                    // Update the "Content" of the register
                    RegisterFile[i][2] = busData[1];
                    break;
                }
            }
    
        }
      
    }
    
    private static void updateMemory(String[] busData){
        Memory[Integer.parseInt(busData[1])] = busData[2];
    }

    private static Boolean Done(){
        for(int i = 1; i < InstructionQueue.length; i++){
            if (InstructionQueue[i][4] == null)
                return false;
        }
        if(Bus.size() > 0)
            return false;
        else    
            return true;
    }

    public static void main(String[] args) {
        //Add Sub Mul Div Load Store
        parser(2,2,10,40,2,2);
        System.out.println("Memory: \n" + Arrays.toString(Memory));
        }
}

