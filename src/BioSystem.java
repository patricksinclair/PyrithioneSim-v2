import com.sun.xml.internal.org.jvnet.mimepull.MIMEConfig;

import java.util.Random;

public class BioSystem {

    Random rand = new Random();

    private int L, K;
    private double alpha, c_max;
    private Microhabitat[] microhabitats;
    private int initialMaxRandPop = 500; // I'll fill the first few microhabitats with a random number (0-maxrandpop) of random bacteria
    private int initialPopZone = 200;
    private double timeElapsed;

    //counters to keep track of the number of events that happen

    private int deathCounter, replicationCounter, immigrationCounter,
            forcedImmigrationCounter, migrationCounter, nothingCounter;


    public BioSystem(int L, int K, double alpha, double c_max){
        this.L = L;
        this.K = K;
        this.alpha = alpha;
        this.timeElapsed = 0.;

        this.microhabitats = new Microhabitat[L];

        for(int i = 0; i < L; i++) {
            microhabitats[i] = new Microhabitat(K, BioSystem.getCValWithOffset(i, c_max, alpha, L));
        }

        microhabitats[L-1].setSurface(true);
        microhabitats[L-1].setBiofilm_region(true);
        microhabitats[L-1].randomlyPopulate(100);

        deathCounter = 0; replicationCounter = 0; immigrationCounter = 0;
        forcedImmigrationCounter = 0; migrationCounter = 0; nothingCounter = 0;

    }

    public double getTimeElapsed(){return timeElapsed;}
    public int getDeathCounter(){return deathCounter;}
    public int getReplicationCounter(){return replicationCounter;}
    public int getImmigrationCounter(){return immigrationCounter;}
    public int getForcedImmigrationCounter(){return forcedImmigrationCounter;}
    public int getMigrationCounter(){return migrationCounter;}
    public int getNothingCounter(){return nothingCounter;}


    public int getTotalN(){
        int runningTotal = 0;

        for(Microhabitat m : microhabitats){
            runningTotal += m.getN();
        }
        return  runningTotal;
    }


    public boolean everythingIsDead(){
        //returns true if the population of the system is 0

        return getTotalN() == 0;
    }

    public int getBiofilmEdge(){
        //finds the microhabitat furthest from the surface which is part of the biofilm
        int edgeIndex = 0;
        for(int i = 0; i < L; i++){
            if(microhabitats[i].getBiofilm_region()){
                edgeIndex = i;
                break;
            }
        }
        return edgeIndex;
    }

    public int[] getPopulationDistribution(){
        int[] popSizes = new int[L];

        for(int i = 0; i < L; i++){
            popSizes[i] = microhabitats[i].getN();
        }
        return popSizes;
    }

    public double[] getAvgGenotypeDistribution(){
        double[] avgGenos = new double[L];
        for(int i = 0; i < L; i++){
            avgGenos[i] = microhabitats[i].getAvgGenotype();
        }
        return avgGenos;
    }



    public int[] getRandIndexes(int randBacteriaNumber){
        int totalCounter = 0;
        int microhab_index = 0;
        int bacteria_index = 0;

        int N_tot = getTotalN();
        // if we pick the bacteria 'outside the system', then we'll see if it gets immigrated
        if(randBacteriaNumber == N_tot) {
            return new int[]{-1, -1};

        } else {

            forloop:
            for(int i = 0; i < L; i++) {
                if(totalCounter + microhabitats[i].getN() <= randBacteriaNumber) {
                    totalCounter += microhabitats[i].getN();
                    continue forloop;

                } else {
                    microhab_index = i;
                    bacteria_index = randBacteriaNumber - totalCounter;
                    break forloop;
                }
            }
            return new int[]{microhab_index, bacteria_index};
        }
    }


    public void migrate(int mh_index, int bac_index){
        //Here this should only move bacteria within the biofilm region
        //takes the arguments of the microhabitat and bacteria indexes, so it performs
        //the necessary action on the desired one
        int biof_edge = getBiofilmEdge();
        double rand_bac = microhabitats[mh_index].getPopulation().get(bac_index);

        if(mh_index == L-1){
            microhabitats[mh_index].removeABacterium(bac_index);
            microhabitats[mh_index-1].addABacterium(rand_bac);

        }else if(mh_index == biof_edge){
            //if the bacteria is at the edge of the biofilm, there's a chance it detached, depending on the stickiness
            //ADD IN LATER
            microhabitats[mh_index].removeABacterium(bac_index);
            microhabitats[mh_index+1].addABacterium(rand_bac);

        }else{
            if(rand.nextBoolean()){
                microhabitats[mh_index].removeABacterium(bac_index);
                microhabitats[mh_index+1].addABacterium(rand_bac);
            }else{
                microhabitats[mh_index].removeABacterium(bac_index);
                microhabitats[mh_index-1].addABacterium(rand_bac);
            }
        }
    }


    public void replicate(int mh_index, int bac_index){
        microhabitats[mh_index].replicateABacterium(bac_index);
    }

    public void die(int mh_index, int bac_index){
        microhabitats[mh_index].removeABacterium(bac_index);
    }

    public void immigrate(int mh_index){
        microhabitats[mh_index].addARandomBacterium();
    }

    public void updateBiofilmSize(){

        for(Microhabitat m : microhabitats){
            if(m.fractionFull() >= m.getThreshold_stickiness()) m.setBiofilm_region(true);
        }
    }


    public void performAction(){
        //TODO add update for finding edge of biofilm
        //TODO should probably be done at the start of performAction

        int N = getTotalN(); //no. of bacteria in the system
        double R_max = 105.2;
        double immigrationRate = 100.;
        boolean nothingHappened = true;

        if(N==0){
            //this forced immigration keeps the no. of bacteria > 0, to prevent division by 0 errors
            microhabitats[L-1].addARandomBacterium();
            forcedImmigrationCounter++;
        }else{

            int randBacIndex = rand.nextInt(getTotalN()+1); //add +1 to allow for the immigration aspect
            int [] randIndexes = getRandIndexes(randBacIndex);
            int microhabIndex = randIndexes[0], bacteriaIndex = randIndexes[1];


            // this is an immigration event //////////////////////////////
            if(microhabIndex < 0){

                if(rand.nextDouble()*R_max <= immigrationRate){
                    microhabitats[getBiofilmEdge()].addARandomBacterium();
                    immigrationCounter++;
                    nothingHappened = false;

                }
                if(nothingHappened) nothingCounter++;

                updateBiofilmSize();
                timeElapsed += 1./(double)N*R_max;
                return;
            }
            //////////////////////////////////////////////////////////////

            //this is for all other events

            System.out.println("N: "+String.valueOf(N)+"\tmh: "+String.valueOf(microhabIndex)+"\tb: "+String.valueOf(bacteriaIndex));
            Microhabitat rand_mh = microhabitats[microhabIndex];

            double replication_rate = rand_mh.replicationRate(bacteriaIndex);
            double migrate_rate = rand_mh.migrate_rate();

            //if statement to handle if replication or death occurs, as -ve signs need to be handled
            if(replication_rate >= 0.){
                double rand_chance = rand.nextDouble()*R_max;

                if(rand_chance <= migrate_rate) {
                    migrate(microhabIndex, bacteriaIndex);
                    migrationCounter++;
                    nothingHappened = false;
                }
                else if(rand_chance > migrate_rate && rand_chance <= replication_rate+migrate_rate) {
                    replicate(microhabIndex, bacteriaIndex);
                    replicationCounter++;
                    nothingHappened = false;
                }

            }else{
                double rand_chance = rand.nextDouble()*R_max;
                replication_rate*=-1.;

                if(rand_chance <= migrate_rate){
                    migrate(microhabIndex, bacteriaIndex);
                    migrationCounter++;
                    nothingHappened = false;
                }
                else if(rand_chance > migrate_rate && rand_chance <= replication_rate+migrate_rate){
                    die(microhabIndex, bacteriaIndex);
                    deathCounter++;
                    nothingHappened = false;
                }
            }
        }

        updateBiofilmSize();
        if(nothingHappened) nothingCounter++;
        timeElapsed += 1./(double)N*R_max;
    }



    public static void getNumberOfEvents(double alpha){

        int K = 500, L = 500;
        int nReps = 4;
        int duration = 500;
        int nCounters = 6;
        double c_max = 10.;
        double interval = duration/20.;

        String filename = "pyrithione-randAlgorithmEvents";

        int[][] allEventCounts = new int[nReps][];

        String[] counterHeaders = {"migration", "immigration", "forced immigr", "replication", "death", "nothing"};


        for(int r = 0; r < nReps; r++){

            BioSystem bs = new BioSystem(L, K, alpha, c_max);

            while(bs.getTimeElapsed() <= duration){

                bs.performAction();
                System.out.println("test");
                double timeElapsed = bs.getTimeElapsed();

                if(timeElapsed%interval >= 0. && timeElapsed%interval <= 0.01){
                    System.out.println("rep: "+String.valueOf(r)+"\ttime: "+String.valueOf(timeElapsed));
                }
            }

            int[] eventCounts = {bs.getMigrationCounter(), bs.getImmigrationCounter(), bs.getForcedImmigrationCounter(),
                    bs.getReplicationCounter(), bs.getDeathCounter(), bs.getNothingCounter()};

            allEventCounts[r] = eventCounts;
        }

        double[] avgResults = Toolbox.averagedResults(allEventCounts);

        Toolbox.writeSimpleArrayAndHeadersToFile(filename, counterHeaders, avgResults);


    }














    public static double getCValWithOffset(int index, double maxC, double alpha, int L){
        //this calculates i* for the gradient profile offset, moves so the final concn is maxC, and it decreases with 1/e
        //or something like that
        //then calculates the corresponding concentration in that microhabitat

        double offset =  (L-1.) - Math.log(maxC+1.)/alpha;

        return (index >= offset) ? Math.exp(alpha*(index - offset)) - 1. : 0.;
    }
}
