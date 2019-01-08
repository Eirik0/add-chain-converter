package addchain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import addchain.RustLine.CommentRustLine;
import addchain.RustLine.LetRustLine;
import addchain.RustLine.MulAssignRustLine;
import addchain.RustLine.SquareAssignRustLine;

public interface AddChainLine {
    static String convertToRust(List<String> lines, boolean verbose) {
        List<RustLine> rustLines = new ArrayList<>();
        for (String line : lines) {
            rustLines.addAll(newChainLine(line).toRustCode(verbose));
        }
        return RustLine.toRustString(rustLines);
    }

    /**
     * Returns an AddChainLine for a given string
     */
    static AddChainLine newChainLine(String line) {
        int indexOfHashTag = line.indexOf("#");
        if (indexOfHashTag == 0) {
            return new ChainLineComment(line.substring(1));
        }
        String trimmed = line.substring(0, indexOfHashTag == -1 ? line.length() : indexOfHashTag).replaceAll("\\s", "");
        String[] split = trimmed.split("=");
        String lhs = split[0];
        String rhs = split[1];
        if (rhs.contains("*")) {
            String[] mulsplit = rhs.split("\\*");
            return new ChainLineAssign(lhs, new RhsMul(mulsplit[0], mulsplit[1]));
        } else if (rhs.startsWith("sqr")) {
            return new ChainLineAssign(lhs, new RhsSquare(rhs.substring(4, rhs.length() - 1)));
        } else {
            return new ChainLineAssign(lhs, new RhsAssign(rhs));
        }
    }

    /**
     * Converts a given AddChainLine to a string of rust code
     */
    List<RustLine> toRustCode(boolean verbose);

    /**
     * A comment
     */
    public static class ChainLineComment implements AddChainLine {
        public final String comment;

        public ChainLineComment(String comment) {
            this.comment = comment;
        }

        @Override
        public List<RustLine> toRustCode(boolean verbose) {
            return Collections.singletonList(new CommentRustLine(comment));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            return prime + comment.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ChainLineComment other = (ChainLineComment) obj;
            return comment.equals(other.comment);
        }

        @Override
        public String toString() {
            return "Comment: " + comment;
        }
    }

    /**
     * A statement of the form lhs = _
     */
    public static class ChainLineAssign implements AddChainLine {
        public final String lhs;
        public final ChainLineAssignRhs rhs;

        public ChainLineAssign(String lhs, ChainLineAssignRhs rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public List<RustLine> toRustCode(boolean verbose) {
            return rhs.toRustCode(lhs, verbose);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            return prime * (prime + lhs.hashCode()) + rhs.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ChainLineAssign other = (ChainLineAssign) obj;
            return rhs.equals(other.rhs) && lhs.equals(other.lhs);
        }

        @Override
        public String toString() {
            return lhs + " = " + rhs.toString();
        }
    }

    /**
     * The right hand side of an assignment
     */
    public interface ChainLineAssignRhs {
        List<RustLine> toRustCode(String lhs, boolean verbose);
    }

    /**
     * _ = rhs
     */
    public static class RhsAssign implements ChainLineAssignRhs {
        public final String rhs;

        public RhsAssign(String rhs) {
            this.rhs = rhs;
        }

        @Override
        public List<RustLine> toRustCode(String lhs, boolean verbose) {
            return Collections.singletonList(new LetRustLine(lhs, rhs));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            return prime + rhs.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            RhsAssign other = (RhsAssign) obj;
            return rhs.equals(other.rhs);
        }

        @Override
        public String toString() {
            return rhs;
        }
    }

    /**
     * _ = mul1 * mul2
     */
    public static class RhsMul implements ChainLineAssignRhs {
        public final String mul1;
        public final String mul2;

        public RhsMul(String mul1, String mul2) {
            this.mul1 = mul1;
            this.mul2 = mul2;
        }

        @Override
        public List<RustLine> toRustCode(String lhs, boolean verbose) {
            if (lhs.equals(mul1)) {
                return Collections.singletonList(new MulAssignRustLine(lhs, mul2));
            } else if (lhs.equals(mul2)) {
                return Collections.singletonList(new MulAssignRustLine(lhs, mul1));
            } else {
                if (verbose) {
                    return Arrays.asList(new LetRustLine(lhs, mul1),
                            new MulAssignRustLine(lhs, mul2));
                } else {
                    return Collections.singletonList(new LetRustLine(lhs, mul1 + " * &" + mul2));
                }
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            return prime * (prime + mul1.hashCode()) + mul2.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            RhsMul other = (RhsMul) obj;
            return mul1.equals(other.mul1) && mul2.equals(other.mul2);
        }

        @Override
        public String toString() {
            return mul1 + " * " + mul2;
        }
    }

    /**
     * _ = sq^2
     */
    public static class RhsSquare implements ChainLineAssignRhs {
        public final String sq;

        public RhsSquare(String sq) {
            this.sq = sq;
        }

        @Override
        public List<RustLine> toRustCode(String lhs, boolean verbose) {
            if (lhs.equals(sq)) {
                return Collections.singletonList(new SquareAssignRustLine(lhs));
            } else {
                if (verbose) {
                    return Arrays.asList(new LetRustLine(lhs, sq), new SquareAssignRustLine(lhs));
                } else {
                    return Collections.singletonList(new LetRustLine(lhs, sq + "." + AddChainMain.SQUARE_FUNCTION_STRING + "()"));
                }
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            return prime + sq.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            RhsSquare other = (RhsSquare) obj;
            return sq.equals(other.sq);
        }

        @Override
        public String toString() {
            return "sqr(" + sq + ")";
        }
    }
}
