package addchain;

import java.util.ArrayList;
import java.util.List;

public interface RustLine {
    /**
     * Takes a list of `RustLine`s and returns the equivalent code as a string
     */
    static String toRustString(List<RustLine> rustLines) {
        for (int i = 0; i < rustLines.size(); ++i) {
            RustLine line = rustLines.get(i);
            if (line instanceof LetRustLine) {
                ((LetRustLine) line).maybeSetMutable(rustLines, i + 1);
            }
        }

        List<RustLine> collapsedLines = new ArrayList<>();
        for (int i = 0; i < rustLines.size(); ++i) {
            RustLine line = rustLines.get(i);
            int inARow = 0;
            if (line instanceof SquareAssignRustLine) {
                inARow = ((SquareAssignRustLine) line).countInARow(rustLines, i + 1);
            }
            if (inARow > 1) {
                collapsedLines.add(new SquareAssignMultiRustLine(line.getLhs(), inARow));
                i += inARow - 1;
            } else {
                collapsedLines.add(line);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (RustLine collapsedLine : collapsedLines) {
            sb.append(collapsedLine.toRustString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * returns the left hand side of a given line of rust code
     */
    String getLhs();

    /**
     * Converts a `RustLine` to a line of rust code
     */
    String toRustString();

    /**
     * A comment
     */
    public static class CommentRustLine implements RustLine {
        String comment;

        public CommentRustLine(String comment) {
            this.comment = comment;
        }

        @Override
        public String getLhs() {
            return "";
        }

        @Override
        public String toRustString() {
            return "//" + comment;
        }
    }

    /**
     * An assignment using `let`
     */
    public static class LetRustLine implements RustLine {
        public final String lhs;
        public final String rhs;

        public boolean mutable;

        public LetRustLine(String lhs, String rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public String getLhs() {
            return lhs;
        }

        @Override
        public String toRustString() {
            return "let" + (mutable ? " mut " : " ") + lhs + " = " + rhs + ";";
        }

        public void maybeSetMutable(List<RustLine> rustLines, int startIndex) {
            for (int i = startIndex; i < rustLines.size(); ++i) {
                RustLine line = rustLines.get(i);
                if (lhs.equals(line.getLhs())) {
                    // Set mutable if a subsequent line is anything other than a let
                    mutable = !(line instanceof LetRustLine);
                    return;
                }
            }
        }
    }

    /**
     * A call to mul_assign
     */
    public static class MulAssignRustLine implements RustLine {
        public final String lhs;
        public final String rhs;

        public MulAssignRustLine(String lhs, String rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public String getLhs() {
            return lhs;
        }

        @Override
        public String toRustString() {
            return lhs + ".mul_assign(&" + rhs + ");";
        }
    }

    /**
     * A call to square_assign()
     */
    public static class SquareAssignRustLine implements RustLine {
        public final String lhs;

        public SquareAssignRustLine(String lhs) {
            this.lhs = lhs;
        }

        public int countInARow(List<RustLine> rustLines, int startIndex) {
            int inARow = 1;
            for (int i = startIndex; i < rustLines.size(); ++i) {
                RustLine line = rustLines.get(i);
                // Accumulate while we have multiple square_assigns in a row
                if (line instanceof SquareAssignRustLine && lhs.equals(line.getLhs())) {
                    ++inARow;
                } else {
                    break;
                }
            }
            return inARow;
        }

        @Override
        public String getLhs() {
            return lhs;
        }

        @Override
        public String toRustString() {
            return lhs + " = " + lhs + ".square();";
        }
    }

    public static class SquareAssignMultiRustLine implements RustLine {
        public final String lhs;
        public final int inARow;

        public SquareAssignMultiRustLine(String lhs, int inARow) {
            this.lhs = lhs;
            this.inARow = inARow;
        }

        @Override
        public String getLhs() {
            return lhs;
        }

        @Override
        public String toRustString() {
            return AddChainMain.SQUARE_ASSIGN_MULTI_FUNCTION_STRING + "(&mut " + lhs + ", " + inARow + ");";
        }
    }
}