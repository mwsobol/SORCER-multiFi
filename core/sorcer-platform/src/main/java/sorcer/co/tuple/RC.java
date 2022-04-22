package sorcer.co.tuple;

// specify the size of a matrix
public class RC extends Tuple2<Integer, Integer> {

    public boolean rowMajor = true;

    public RC(int x1, int x2) {
        _1 = x1;
        _2 = x2;
    }

    public RC(int x1, int x2, boolean rowMajor) {
        _1 = x1;
        _2 = x2;
        this.rowMajor = rowMajor;
    }

    public int numRows(int numRows) {
        return _1 = numRows;
    }

    public int numCols(int numCols) {
        return _2 = numCols;
    }

    public int numRows() {
        return _1;
    }

    public int numCols() {
        return _2;
    }

    public boolean rowMajor() {
        return rowMajor;
    }

    public void rowMajor(boolean rowMajor) {
        this.rowMajor = rowMajor;
    }
}
