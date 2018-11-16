import org.apache.commons.math3.distribution.LogNormalDistribution;

import java.util.ArrayList;
import java.util.Random;

public class Microhabitat {

    //stuff for lognormal distribution of MICs
    private double mu = Math.log(7.92016113), sigma = 0.10018864;
    private LogNormalDistribution MIC_distribution = new LogNormalDistribution(mu, sigma);

    Random rand = new Random();
    private int K; //karrying kapacity
    private double c; //concentration of antimicrobial
    ArrayList<Double> population; //contains a list of doubles representing the MICs of the bacteria present


    public Microhabitat(int K, double c){
        this.K = K;
        this.c = c;
        this.population = new ArrayList<>(K);
    }


    public int getN(){return population.size();}
    public double getC(){return c;}

    public double fractionFull(){
        return (double)getN()/(double)K;
    }


    public double beta(int index){
        return population.get(index);
    }

    public double phi_c(int index){
        double phi_c = 1. - (c/beta(index))*(c/beta(index));
        return  (phi_c > 0.) ? phi_c : 0.;
    }

    public double replicationRate(int index){
        double gRate = phi_c(index)*(1. - getN()/K);
        return  (gRate > 0.) ? gRate : 0.;
    }






    public void randomlyPopulate(){

        for(int i = 0; i < K; i++){
            population.add(MIC_distribution.sample());
        }
    }


    public void addARandomBacterium(){
        population.add(MIC_distribution.sample());
    }

    public void replicateABacterium(int index){
        population.add(population.get(index));
    }

    public void removeABacterium(int index){
        population.remove(index);
    }





}
