import java.util.Random;
import java.math.*;

public class PrimeGenerator {

	private int bit_length;
	private Random rand;
	private int certainty;
	static private BigInteger one = new BigInteger("1");
	static private BigInteger two = new BigInteger("2");
	private BigInteger q;
	private BigInteger p;
	private BigInteger g;
	
	public PrimeGenerator(int length, int cert) {
		bit_length = length;
		certainty = cert;
		rand = new Random();
		generate();
		primitiveRoot();
	}
	
	public static void main(String[] args) {
		PrimeGenerator primes = new PrimeGenerator(512, 3);
	}
	
	public void generate() {
		
		boolean prime = false;
		
		while (!prime) {
			q = new BigInteger(bit_length, certainty, rand);
			p = q.multiply(two);
			
			p = p.add(one);
			
			System.out.println("p is : " + p.toString());
			System.out.println("q is : " + q.toString());
			
			prime = p.isProbablePrime(certainty);
		}
		
	}
	
	public void primitiveRoot() {
		for (g = BigInteger.ONE; g.compareTo(p.subtract(one)) < 0; g = g.add(BigInteger.ONE)) {
			if (  g.modPow(q,p).compareTo(BigInteger.ONE) > 0) {
				break;
			}
		}
		System.out.println("primitive root g: " + g.toString());
	}
	
	public BigInteger getQ() {
		return q;
	}
	public BigInteger getP() {
		return p;
	}
	public BigInteger getG() {
		return g;
	}
}
