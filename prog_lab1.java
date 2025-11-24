public class prog_lab1 {
    public static void main(String[] args) {
        long w[] = new long[10];
        long value =19;
        for (int i = 0; i<10; i = i +1, value -=2) {
            w[i] = value;

        }
        double x[] = new double[16];
        for (int i =0; i<x.length; i++) {
            x[i] = -11.0 + (Math.random() * 8.00);
        }
        double n[][] = new double[10][16];
        for (int i =0; i<10; i++) {
            for (int j =0; j<16; j++) {
                n[i][j] = element(w[i], x[j]);

            }
        }
        printArray(n);

    }

    static double element (long W, double X) {
        if (W == 17) {
            return Math.log(Math.acos(Math.sin(X)));
        }
        else if (W == 1 | W == 5 | W == 7 | W == 15 | W == 19 ) {
            return Math.exp(Math.exp(Math.pow(X, X)));
        }
        else {
            return Math.pow(Math.exp(Math.pow(Math.pow(X, X - 0.75), ((Math.log(Math.abs(X)) -4)/4))) - 0.5, 3);
        }
    }
    static void printArray(double n[][]) {
        for (int i=0; i<10; i++) {
            for (int j=0; j<16; j++) {
                System.out.format("%10.4f\t", n[i][j]);
            }
        }
    } 
}