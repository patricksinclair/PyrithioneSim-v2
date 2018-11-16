import java.util.Random;

public class BioSystem {

    Random rand = new Random();

    private int L, K;
    private double alpha;
    Microhabitat[] microhabitats;
    private int initialMaxRandPop = 500; // I'll fill the first few microhabitats with a random number (0-maxrandpop) of random bacteria
    private int initialPopZone


















    public static double getCValWithOffset(int index, double maxC, double alpha, int L){
        //this calculates i* for the gradient profile offset, moves so the final concn is maxC, and it decreases with 1/e
        //or something like that
        //then calculates the corresponding concentration in that microhabitat

        double offset =  (L-1.) - Math.log(maxC+1.)/alpha;

        return (index >= offset) ? Math.exp(alpha*(index - offset)) - 1. : 0.;
    }
}
