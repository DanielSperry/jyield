import jyield.Continuable;
import jyield.Yield;

public class Sample {
        
        @Continuable
        public static Iterable<Integer> power(int number, int exponent) {
                int counter = 0;
                int result = 1;
                while (counter++ < exponent) {
                        result = result * number;
                        System.out.print("[" + result+ "]");
                        Yield.ret(result);
                }
                return Yield.done();
        }

        public static void main(String[] args) {
                // Display powers of 2 up to the exponent 8:
                for (int i : power(2, 8)) {
                        System.out.print(" "+ i + " ");
                }
        }
}