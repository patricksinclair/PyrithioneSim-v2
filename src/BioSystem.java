import com.sun.xml.internal.org.jvnet.mimepull.MIMEConfig;

import java.util.Random;

public class BioSystem {

    Random rand = new Random();

    private int L, K;
    private double alpha, c_max;
    Microhabitat[] microhabitats;
    private int initialMaxRandPop = 500; // I'll fill the first few microhabitats with a random number (0-maxrandpop) of random bacteria
    private int initialPopZone = 200;
    private double timeElapsed;


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

    }

    public double getTimeElapsed(){return timeElapsed;}


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

        forloop:
        for(int i = 0; i < L; i++){
            if(totalCounter + microhabitats[i].getN() <= randBacteriaNumber){
                totalCounter += microhabitats[i].getN();
                continue forloop;

            }else{
                microhab_index = i;
                bacteria_index = randBacteriaNumber - totalCounter;
                break forloop;
            }
        }
        return new int[]{microhab_index, bacteria_index};
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


    public void performAction(){

        int N = getTotalN(); //no. of bacteria in the system
        double R_max = 5.2;

        if(N==0){
            microhabitats[L-1].addARandomBacterium();
        }else{

            int randBacIndex = rand.nextInt(getTotalN());
            int [] randIndexes = getRandIndexes(randBacIndex);
            int microhabIndex = randIndexes[0], bacteriaIndex = randIndexes[1];
            Microhabitat rand_mh = microhabitats[microhabIndex];

            double replication_rate = rand_mh.replicationRate(bacteriaIndex);
            double migrate_rate = rand_mh.migrate_rate();

            //if statement to handle if replication or death occurs, as -ve signs need to be handled
            if(replication_rate >= 0.){
                double rand_chance = rand.nextDouble()*R_max;

                if(rand_chance <= migrate_rate) migrate(microhabIndex, bacteriaIndex);
                else if(rand_chance > migrate_rate && rand_chance <= replication_rate+migrate_rate) replicate(microhabIndex, bacteriaIndex);

            }else{
                double rand_chance = rand.nextDouble();
                replication_rate*=-1.;

                if(rand_chance <= migrate_rate) migrate(microhabIndex, bacteriaIndex);
                else if(rand_chance > migrate_rate && rand_chance <= replication_rate+migrate_rate) die(microhabIndex, bacteriaIndex);
            }
        }

        timeElapsed += 1./(double)N*R_max;
    }
















    public static double getCValWithOffset(int index, double maxC, double alpha, int L){
        //this calculates i* for the gradient profile offset, moves so the final concn is maxC, and it decreases with 1/e
        //or something like that
        //then calculates the corresponding concentration in that microhabitat

        double offset =  (L-1.) - Math.log(maxC+1.)/alpha;

        return (index >= offset) ? Math.exp(alpha*(index - offset)) - 1. : 0.;
    }
}
