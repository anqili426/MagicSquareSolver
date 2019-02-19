package test;

import java.util.HashMap;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

public class MagicSquare {
	public static final int N = 5;

	public static void magicSquare(Context ctx) {

		// 3 * 3 int matrix
		IntExpr[][] X = new IntExpr[N][];
		for (int i = 0; i < N; i++) {
			X[i] = new IntExpr[N];
			for (int j = 0; j < N; j++) {
				X[i][j] = (IntExpr) ctx.mkConst(ctx.mkSymbol("x_" + (i + 1) + "_" + (j + 1)), ctx.getIntSort());
			}
		}

		// Cell value from 1 - 9
		BoolExpr[][] cells = new BoolExpr[N][]; 
		for (int i = 0; i < N; i++) {
			cells[i] = new BoolExpr[N];
			for (int j = 0; j < N; j++) {
				cells[i][j] =  ctx.mkAnd(ctx.mkLe(ctx.mkInt(1), X[i][j]), ctx.mkLe(X[i][j], ctx.mkInt(N*N)));
			}
		}

		// Each value in the 3*3 square is distinct 
		IntExpr[] square_1D = new IntExpr[N * N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				square_1D[i * N + j] = X[i][j]; 
			}
		}
		BoolExpr square_distinct = ctx.mkDistinct(square_1D);

		// Sum of values in every row should be equal
		ArithExpr[] sum_r = new ArithExpr[N];
		BoolExpr[] row_equal_sum = new BoolExpr[N - 1];
		for (int i = 0; i < N; i++) {
			sum_r[i] = ctx.mkAdd(X[i]);
			if (i > 0) {
				row_equal_sum[i - 1] = ctx.mkEq(sum_r[i], sum_r[i - 1]);
			}
		}	

		// Sum of values in every col should be equal
		ArithExpr[] sum_c = new ArithExpr[N];
		BoolExpr[] col_equal_sum = new BoolExpr[N - 1];
		
		IntExpr[][] cols = new IntExpr[N][];
		for (int i = 0; i < N; i++) {
			cols[i] = new IntExpr[N];
			for (int j = 0; j < N; j++) {
				cols[i][j] = X[j][i];
			}
		}
		
		for (int i = 0; i < N; i++) {
			sum_c[i] = ctx.mkAdd(cols[i]);
			if (i > 0) {
				col_equal_sum[i - 1] = ctx.mkEq(sum_c[i], sum_c[i - 1]);
			}
		}
		
		BoolExpr r_c_eq = ctx.mkEq(sum_r[0], sum_c[0]);
	
		// Sum of values in every diagonal should be equal
		ArithExpr[] sum_d = new ArithExpr[2];

		IntExpr[] one_diag = new IntExpr[N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (i == j) {
					one_diag[i] = X[i][j];
					break;
				}
			}
		}
		sum_d[0] = ctx.mkAdd(one_diag);

		IntExpr[] two_diag = new IntExpr[N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if ((j + i) == (N - 1)) {
					two_diag[i] = X[i][j];
					break;
				}
			}
		}
		sum_d[1] = ctx.mkAdd(two_diag);

		BoolExpr diag_equal_sum = ctx.mkEq(sum_d[0], sum_d[1]);
		BoolExpr r_d_eq = ctx.mkEq(sum_r[0], sum_d[0]);

		// Combine all conditions
		BoolExpr all_cond = ctx.mkTrue();
		// Each value in a cell ranges from [1 - 9]
		for (BoolExpr[] e: cells) {
			all_cond = ctx.mkAnd(ctx.mkAnd(e), all_cond);
		} 
		// No repetitive values
		all_cond = ctx.mkAnd(square_distinct, all_cond);
		// Sum of every row is the same
		all_cond = ctx.mkAnd(ctx.mkAnd(row_equal_sum), all_cond);
		// Sum of every column is the same
		all_cond = ctx.mkAnd(ctx.mkAnd(col_equal_sum), all_cond);
		// Sum of every diagonal is the same
		all_cond = ctx.mkAnd(diag_equal_sum, all_cond);
		// Row sum == col sum
		all_cond = ctx.mkAnd(r_c_eq, all_cond);
		// Row sum == diag sum
		all_cond = ctx.mkAnd(r_d_eq, all_cond);

		Solver s = ctx.mkSolver();
		s.add(all_cond);

		if (s.check() == Status.SATISFIABLE) {
			Model m = s.getModel();
			Expr[][] solution = new Expr[N][N];
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < N; j++) {
					solution[i][j] = m.evaluate(X[i][j], false);
				}
			}

			System.out.println("Magic square solution:\n");
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < N; j++) {
					System.out.print(solution[i][j] + " ");
				}
				System.out.println();
			}
		}
		else {
			System.out.println("No solution");
		}
	}

	public static void main(String[] args) {
		HashMap<String, String> cfg = new HashMap<String, String>();
		cfg.put("model", "true");
		Context ctx = new Context(cfg);
		magicSquare(ctx);
	}

}
