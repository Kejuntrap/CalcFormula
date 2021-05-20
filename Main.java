import java.util.*;

class Main {
  public enum Operands { // 演算子
    // (オペランドの形、結合性、優先度)
    // 結合性 0 多分かんたんにするなら結合性のオペランドも左結合性として扱うのが楽だと思われる
    // 左結合性 -1
    // 右結合性 1
    // その他 2
    PLUS("+", 1, 100), MINUS("-", -1, 100), MULT("*", 1, 200), DIV("/", -1, 200), OPENPARENSIS("(", 2, -1),
    CLOSEPARENSIS(")", 2, -2), POW("^", 1, 300), NUMBER(".", 2, 100000); // 数字もオペランドということにする

    private final String str;
    private final int connectivity;
    private final int priority;

    private Operands(final String s, final int t, final int p) {
      this.str = s;
      this.connectivity = t;
      this.priority = p;
    }

    public String getString() {
      return this.str;
    }

    public int getPriority() {
      return this.priority;
    }

    public int getConnectivity() {
      return this.connectivity;
    }
  }

  public static void main(String[] args) throws DenominatorIsZeroException {
    // Scanner sc = new Scanner(System.in);
    /*
     * while (true) { String shiki = sc.nextLine(); // 空白は除くようにする if
     * (shiki.equals("end")) { break; } else { try { Rational res =
     * evalformula(shiki); } catch (DenominatorIsZeroException e) {
     * System.out.println(e.getMessage()); } } }
     */

    for (int i = 0; i < 42; i++) {
      String shiki = sqrt2(i);
      try {
        Rational res = evalformula(shiki);
      } catch (DenominatorIsZeroException e) {
        System.out.println(e.getMessage());
      }
    }
    // sc.close();
  }

  public static Rational evalformula(String s) throws DenominatorIsZeroException {
    s = trimformula(s);
    // System.out.println(s);
    String[] splitFormula = s.split(" ");
    String[] res = convFormula(splitFormula);
    try {
      Rational r = calcFormula(res);
      System.out.println("" + r.bunshi + "/" + r.bumbo);
      return r;
    } catch (DenominatorIsZeroException e) {
      System.out.println(e.getMessage());
      return new Rational(-1L);
    }
  }

  public static Rational calcFormula(String[] s) throws DenominatorIsZeroException { // 式を計算して答えを出す
    if (s.length == 0) {
      return new Rational(0L); // なにもない式も四季として成立(式のフォーマットからは離れていないという点で)
    } else {
      ArrayList<Rational> rs = new ArrayList<Rational>();
      for (int i = 0; i < s.length; i++) { // 操車場アルゴリズムによって中置記法を後置記法に変換し、スタックで計算している
        if (notOperand(s[i])) {
          rs.add(new Rational(s[i]));
        } else {
          if (s[i].equals(Operands.PLUS.getString())) {
            Rational r2 = rs.remove(rs.size() - 1);
            Rational r1 = rs.remove(rs.size() - 1);
            rs.add(Rational.plus(r1, r2));
          } else if (s[i].equals(Operands.MINUS.getString())) {
            Rational r2 = rs.remove(rs.size() - 1);
            Rational r1 = rs.remove(rs.size() - 1);
            rs.add(Rational.minus(r1, r2));
          } else if (s[i].equals(Operands.MULT.getString())) {
            Rational r2 = rs.remove(rs.size() - 1);
            Rational r1 = rs.remove(rs.size() - 1);
            rs.add(Rational.mult(r1, r2));
          } else if (s[i].equals(Operands.DIV.getString())) {
            Rational r2 = rs.remove(rs.size() - 1);
            Rational r1 = rs.remove(rs.size() - 1);
            rs.add(Rational.div(r1, r2));
          }
        }
      }
      return new Rational(rs.get(0));
    }

  }

  public static boolean notOperand(String s) {
    Operands[] ops = Operands.values();
    for (Operands a : ops) {
      if (a.getString().equals(s)) {
        return false;
      }
    }
    return true;
  }

  public static String[] convFormula(String[] s) { // ここでは左結合性のオペランドのみで構成されるものとする
    // Shuntingyard algorithm
    ArrayDeque<String> outputs = new ArrayDeque<String>(); // 出力
    ArrayDeque<String> detentstack = new ArrayDeque<String>(); // 留置するスタック

    for (int i = 0; i < s.length; i++) {
      if (getpriority(s[i]) == Operands.NUMBER.getPriority()) { // num
        outputs.addLast(s[i]); // 問答無用でアウトプットスタックに入れていたが吟味…しなくても良さそう
        // detentstack.addLast(s[i]);
      } else if (getpriority(s[i]) == Operands.MULT.getPriority() || getpriority(s[i]) == Operands.DIV.getPriority()
          || getpriority(s[i]) == Operands.PLUS.getPriority() || getpriority(s[i]) == Operands.MINUS.getPriority()) { // 左結合性の算術記号
        while (detentstack.size() > 0 && getpriority(detentstack.getLast()) >= getpriority(s[i])) {
          outputs.addLast(detentstack.removeLast());
        }
        detentstack.addLast(s[i]);
      } else if (getpriority(s[i]) == Operands.OPENPARENSIS.getPriority()) { // 開きカッコ
        detentstack.addLast(s[i]);
      } else if (getpriority(s[i]) == Operands.CLOSEPARENSIS.getPriority()) { // 閉じカッコ
        while (detentstack.size() > 0 && (getpriority(detentstack.getLast()) != Operands.OPENPARENSIS.getPriority())) { // 閉じ括弧がきて開き括弧がくるまでオペランドを出力のスタックに積む
          outputs.addLast(detentstack.removeLast());
        }
        if (detentstack.size() > 0 && getpriority(detentstack.getLast()) == Operands.OPENPARENSIS.getPriority()) { // 閉じ括弧に対応する開き括弧が来たら開き括弧をスタックから取り除く
          detentstack.removeLast();
        }
      }
    }
    while (detentstack.size() > 0) { // 残ったオペランドスタックの中身をすべて出す
      outputs.add(detentstack.removeLast());
    }
    Object[] ret = new Object[outputs.size()];
    ret = outputs.toArray();
    String[] r = new String[outputs.size()];
    for (int i = 0; i < outputs.size(); i++) {
      r[i] = (String) ret[i];
    }
    return r;
  }

  public static int getpriority(String op) {
    if (op.equals(Operands.PLUS.getString()) || op.equals(Operands.MINUS.getString())) { // low priority operand
      return Operands.PLUS.getPriority(); // + -の優先度
    } else if (op.equals(Operands.MULT.getString()) || op.equals(Operands.DIV.getString())) { // high priority operand
      return Operands.MULT.getPriority(); // * /の優先度
    } else if (op.equals(Operands.OPENPARENSIS.getString())) { // 括弧はオペランド扱いとした時に最も優先度が低いと扱うとうまくいくはず
      return Operands.OPENPARENSIS.getPriority();
    } else if (op.equals(Operands.CLOSEPARENSIS.getString())) {
      return Operands.CLOSEPARENSIS.getPriority();
    } else {
      return Operands.NUMBER.getPriority();
    }
  }

  public static class Rational {
    long bunshi, bumbo;

    Rational(long bs, long bb) throws DenominatorIsZeroException {
      bunshi = bs;
      bumbo = bb;
      if (bumbo == 0L) {
        throw new DenominatorIsZeroException("Denominator is Zero! at decleation two long variants.");
      }
    }

    Rational(Rational r) throws DenominatorIsZeroException {
      bunshi = r.bunshi;
      bumbo = r.bumbo;
      if (bumbo == 0L) {
        throw new DenominatorIsZeroException("Denominator is Zero! at decleation. rational");
      }
    }

    Rational(String s) {
      bunshi = Long.parseLong(s);
      bumbo = 1L;
    }

    Rational(long l) {
      bunshi = l;
      bumbo = 1L;
    }

    public static Rational plus(Rational r1, Rational r2) throws DenominatorIsZeroException {
      if (r1.bumbo * r2.bumbo == 0) {
        throw new DenominatorIsZeroException("Denominator is Zero! at plus func.");
      } else {
        return (redu(new Rational(r1.bunshi * r2.bumbo + r2.bunshi * r1.bumbo, r1.bumbo * r2.bumbo)));
      }
    }

    public static Rational minus(Rational r1, Rational r2) throws DenominatorIsZeroException {
      if (r1.bumbo * r2.bumbo == 0) {
        throw new DenominatorIsZeroException("Denominator is Zero! at minus func.");
      } else {
        return redu(new Rational(r1.bunshi * r2.bumbo - r2.bunshi * r1.bumbo, r1.bumbo * r2.bumbo));
      }
    }

    public static Rational mult(Rational r1, Rational r2) throws DenominatorIsZeroException {
      if (r1.bumbo * r2.bumbo == 0) {
        throw new DenominatorIsZeroException("Denominator is Zero! at mult func.");
      } else {
        return redu(new Rational(r1.bunshi * r2.bunshi, r1.bumbo * r2.bumbo));
      }
    }

    public static Rational div(Rational r1, Rational r2) throws DenominatorIsZeroException {
      if ((isNegative(r1) && isNegative(r2)) || (!isNegative(r1) && !isNegative(r2))) {
        r1 = abs(r1);
        r2 = abs(r2);
        if (r1.bumbo * r2.bunshi == 0) {
          throw new DenominatorIsZeroException("Denominator is Zero! at div func.");
        } else {
          return redu(new Rational(r1.bunshi * r2.bumbo, r1.bumbo * r2.bunshi));
        }
      } else {
        r1 = abs(r1);
        r2 = abs(r2);
        if (r1.bumbo * r2.bunshi == 0) {
          throw new DenominatorIsZeroException("Denominator is Zero! ar div func.");
        } else {
          return redu(new Rational(-1L * r1.bunshi * r2.bumbo, r1.bumbo * r2.bunshi));
        }
      }
    }

    public static long gcd(long r1, long r2) {
      long r;

      r = r1 % r2;
      while (r > 0) {
        r1 = r2;
        r2 = r;
        r = r1 % r2;
      }
      return r2;
    }

    public static Rational redu(Rational r) throws DenominatorIsZeroException { // 約分や分子にのみマイナスを付けるように符号を付け直したりする
      long gcd = gcd(r.bumbo, r.bunshi);
      if (gcd == 0L) {
        throw new DenominatorIsZeroException("Denominator is Zero! at redu func.");
      } else {
        Rational ret = new Rational(r.bunshi / gcd, r.bumbo / gcd);
        if ((ret.bunshi >= 0 && ret.bumbo > 0) || (ret.bunshi <= 0 && ret.bumbo < 0)) { // positive
          ret.bumbo = Math.abs(ret.bumbo);
          ret.bunshi = Math.abs(ret.bunshi);
        } else {
          ret.bumbo = Math.abs(ret.bumbo);
          ret.bunshi = -Math.abs(ret.bunshi);
        }
        return ret;
      }
    }

    public static Rational abs(Rational r) throws DenominatorIsZeroException {
      if (r.bumbo == 0) {
        throw new DenominatorIsZeroException("Denominator is Zero! at abs func.");
      } else {
        return new Rational(Math.abs(r.bunshi), Math.abs(r.bumbo));
      }
    }

    public static boolean isNegative(Rational r) throws DenominatorIsZeroException {
      if (r.bunshi == 0) {
        throw new DenominatorIsZeroException("Denominator is Zero! at isNegative func.");
      } else {
        if (r.bunshi > 0 && r.bumbo < 0) {
          return true;
        } else if (r.bunshi < 0 && r.bumbo > 0) {
          return true;
        } else {
          return false;
        }
      }
    }
  }

  public static boolean isZerodiv(Rational r) {
    if (r.bumbo == 0) {
      return true;
    }
    return false;
  }

  public static class DenominatorIsZeroException extends Exception {
    private static final long serialVersionUID = 1L;

    DenominatorIsZeroException(String s) {
      super(s);
    }
  }

  public static String trimformula(String s) {
    s = s.replaceAll(" ", "").replaceAll("\\{", "(").replaceAll("\\[", "(").replaceAll("\\}", ")").replaceAll("\\]",
        ")"); // 中括弧、大括弧はともに小括弧の連続を許すことで書き換えが可能
    String formattedFormula = "";
    if (!checkvalid(s)) {
      System.out.println("invalid");
      return "0";
    } else {
      System.out.println("valid");
      boolean isOperand = true;
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < s.length(); i++) {
        if (!isOperand && (s.charAt(i) == '+' || s.charAt(i) == '-' || s.charAt(i) == '*' || s.charAt(i) == '/')) {
          isOperand = true; // 前が記号でない時
          sb.append(" " + s.charAt(i) + " ");
        } else if (isOperand && s.charAt(i) == '-') {
          isOperand = false;
          sb.append("-");
        } else if (s.charAt(i) == '(') {
          sb.append("( ");
        } else if (s.charAt(i) == ')') {
          sb.append(" )");
        } else {
          isOperand = false;
          sb.append(s.charAt(i));
        }
      }
      formattedFormula = sb.toString();
    }
    return formattedFormula;
  }

  public static boolean checkvalid(String s) { // havent finished test passed.
    int openParensis = 0;
    int closeParensis = 0;
    boolean isOperand = false;
    for (int i = 0; i < s.length(); i++) { // 閉じ括弧開き括弧の数が等しいかどうか
      if (s.charAt(i) == '(') {
        openParensis++;
      }
      if (s.charAt(i) == ')') {
        closeParensis++;
      }
    }
    if (openParensis != closeParensis) {
      return false;
    } else {
      for (int i = 0; i < s.length(); i++) {
        if (!isOperand) {
          if (s.charAt(i) == '+' || s.charAt(i) == '-' || s.charAt(i) == '*' || s.charAt(i) == '/'
              || s.charAt(i) == '^') {
            isOperand = true;
          } else { // number or parensis
            //
          }
        } else if (isOperand) {
          if (s.charAt(i) == '-') {
            //
          } else if (s.charAt(i) == '+' || s.charAt(i) == '*' || s.charAt(i) == '/' || s.charAt(i) == '^') {
            return false; // 6- *8は正しくない形式
          } else {
            isOperand = false;
          }
        }
      }
      if (isOperand) {
        return false;
      }
    }
    return true;
  }

  public static String sqrt2(int itr) {
    String po = "2+1/2";
    for (int i = 0; i < itr; i++) {
      po = "2+1/(" + po + ")";
    }
    po = "1+1/(" + po + ")";
    return po;
  }
}
