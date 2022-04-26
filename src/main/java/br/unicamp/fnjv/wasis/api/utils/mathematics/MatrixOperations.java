package br.unicamp.fnjv.wasis.api.utils.mathematics;

/**
 * Perform matrix operations.
 * 
 * @author Leandro Tacioli
 */
public class MatrixOperations {

	/**
	 * Perform matrix operations.
	 */
	private MatrixOperations() {
		
	}

	/**
	 * Matrix multiplication method.
	 *
	 * @param matrixA
	 * @param matrixB
	 * 
	 * @return finalMatrix
	 */
    public static double[][] multiplyMatrices(double[][] matrixA, double[][] matrixB) {
        int matrixAColumnLength = matrixA[0].length;
        int matrixBRowLength = matrixB.length;
        
        // Matrix multiplication is not possible
        if (matrixAColumnLength != matrixBRowLength) {
        	return null;
        }
        
        int finalRowLength = matrixA.length;
        int finalColumnLength = matrixB[0].length;

        double[][] finalMatrix = new double[finalRowLength][finalColumnLength];
        
        for (int i = 0; i < finalRowLength; i++) {                    // Rows from Matrix A
            for (int j = 0; j < finalColumnLength; j++) {             // Columns from Matrix B
                for (int k = 0; k < matrixAColumnLength; k++) {       // Columns from Matrix A
                	finalMatrix[i][j] += matrixA[i][k] * matrixB[k][j];
                }
            }
        }
        
        return finalMatrix;
    }
}