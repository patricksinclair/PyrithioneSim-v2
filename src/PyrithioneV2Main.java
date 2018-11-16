import org.apache.commons.math3.distribution.LogNormalDistribution;

public class PyrithioneV2Main {
    public static void main(String[] args){

        double mu = Math.log(7.92016113), sigma = 0.10018864;
        LogNormalDistribution logNormal = new LogNormalDistribution(mu, sigma);

        System.out.println(logNormal.getNumericalMean());
    }
}
