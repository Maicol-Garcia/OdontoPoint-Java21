package pe.odontopoint.report;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Set;

public final class OdontogramRenderer {
  public static final Color INK = new Color(31, 41, 55),
      YELLOW = new Color(245, 190, 45),
      GREEN = new Color(22, 163, 104);
  private final ReportData history;

  public OdontogramRenderer(ReportData data) {
    history = data;
  }

  public BufferedImage image() {
    var b = new BufferedImage(1400, 620, BufferedImage.TYPE_INT_RGB);
    var g = b.createGraphics();
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, 1400, 620);
    draw(g, 1400, 620, false);
    g.dispose();
    return b;
  }

  private void draw(Graphics2D original, int width, int height, boolean interactive) {
    var g = (Graphics2D) original.create();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    double scale = Math.min(width / 1020.0, height / 470.0);
    double ox = (width - 1020 * scale) / 2;
    g.translate(ox, 0);
    g.scale(scale, scale);

    int[][] rows = {
      {18, 17, 16, 15, 14, 13, 12, 11, 21, 22, 23, 24, 25, 26, 27, 28},
      {55, 54, 53, 52, 51, 61, 62, 63, 64, 65},
      {85, 84, 83, 82, 81, 71, 72, 73, 74, 75},
      {48, 47, 46, 45, 44, 43, 42, 41, 31, 32, 33, 34, 35, 36, 37, 38}
    };
    g.setFont(new Font("SansSerif", Font.PLAIN, 12));
    g.setColor(new Color(100, 116, 139));
    g.drawString("DERECHA DEL PACIENTE", 25, 19);
    g.drawString("IZQUIERDA DEL PACIENTE", 810, 19);
    g.setColor(new Color(220, 230, 234));
    g.drawLine(510, 30, 510, 425);
    for (int r = 0; r < 4; r++) {
      int y = 38 + r * 98;
      int start = (1020 - rows[r].length * 59) / 2;
      for (int j = 0; j < rows[r].length; j++) {
        int t = rows[r][j], x = start + j * 59;
        g.setColor(INK);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString(String.valueOf(t), x + 19, y + 12);
        int cy = y + 25;
        g.setColor(new Color(242, 247, 248));
        var root = new Path2D.Double();
        root.moveTo(x + 10, cy + 38);
        root.curveTo(x + 14, cy + 62, x + 20, cy + 66, x + 26, cy + 46);
        root.curveTo(x + 33, cy + 66, x + 42, cy + 62, x + 44, cy + 38);
        root.closePath();
        g.fill(root);
        g.setColor(new Color(176, 192, 202));
        g.draw(root);
        boolean right = Set.of(1, 4, 5, 8).contains(t / 10);
        String[] ss = {"V", right ? "M" : "D", "L", right ? "D" : "M", "O"};
        int[][][] coords = {
          {{5, 0}, {49, 0}, {37, 12}, {17, 12}},
          {{49, 0}, {49, 42}, {37, 30}, {37, 12}},
          {{49, 42}, {5, 42}, {17, 30}, {37, 30}},
          {{5, 42}, {5, 0}, {17, 12}, {17, 30}},
          {{17, 12}, {37, 12}, {37, 30}, {17, 30}}
        };
        for (int s = 0; s < 5; s++) {
          var poly = new Polygon();
          for (var a : coords[s]) poly.addPoint(x + a[0], cy + a[1]);
          String v = history.teeth.get(t + ":" + ss[s]);
          g.setColor(v == null ? Color.WHITE : color(v));
          g.fill(poly);
          g.setColor(new Color(148, 163, 184));
          g.draw(poly);
        }
      }
    }
    g.setColor(new Color(100, 116, 139));
    g.setFont(new Font("SansSerif", Font.PLAIN, 12));
    g.drawString(
        "Dentición permanente y temporal · FDI · V: vestibular · L: lingual/palatina · M: mesial ·"
            + " D: distal · O: oclusal/incisal",
        25,
        456);
    g.dispose();
  }

  public static Color color(String c) {
    return switch (c) {
      case "AMARILLO" -> YELLOW;
      case "VERDE" -> GREEN;
      default -> INK;
    };
  }
}
