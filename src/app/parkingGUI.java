package app;

import model.parking;
import model.voiture;
import threads.mythread;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.Timer;

public class parkingGUI extends JFrame {

    private static final Color BG        = new Color(15, 18, 25);
    private static final Color CARD      = new Color(24, 28, 38);
    private static final Color BORDER    = new Color(45, 50, 65);
    private static final Color ACCENT    = new Color(82, 196, 148);
    private static final Color ACCENT2   = new Color(99, 120, 220);
    private static final Color DANGER    = new Color(220, 90, 80);
    private static final Color MUTED     = new Color(100, 110, 135);
    private static final Color TXT       = new Color(230, 232, 240);
    private static final Color TXT2      = new Color(155, 165, 185);

    private parking park;
    private DefaultTableModel tableModel;
    private JLabel lblPlacesDispo, lblPlacesMax, lblListeAttente;
    private JTextArea logArea;
    private JTable placesTable;
    private Timer refreshTimer;

    public parkingGUI() {
        // 1) créer le parking (modifiable via dialog)
        park = new parking("Parking Central", 10, 15.0);
        redirectSystemOut(); // redirige System.out vers le log

        setTitle("Gestion de Parking");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 680);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        refreshTimer = new Timer(1000, e -> rafraichir());
        refreshTimer.start();

        setVisible(true);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout(16, 0));
        h.setBackground(CARD);
        h.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(16, 24, 16, 24)));

        // titre
        JLabel title = label("🚗  Parking Manager", 20, Font.BOLD, ACCENT);
        h.add(title, BorderLayout.WEST);

        // stats rapides
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        stats.setOpaque(false);
        lblPlacesMax    = label("Max: " + park.getNbr_places_max(), 13, Font.PLAIN, TXT2);
        lblPlacesDispo  = label("Dispo: " + park.getNbr_places_dispo(), 13, Font.BOLD, ACCENT);
        lblListeAttente = label("File: 0", 13, Font.PLAIN, ACCENT2);
        stats.add(lblPlacesMax);
        stats.add(lblPlacesDispo);
        stats.add(lblListeAttente);
        h.add(stats, BorderLayout.EAST);

        return h;
    }

    private JSplitPane buildCenter() {
        JPanel left = new JPanel(new BorderLayout(0, 0));
        left.setBackground(BG);
        left.setBorder(new EmptyBorder(16, 16, 0, 8));

        JLabel lbl = label("PLACES ACTUELLES", 11, Font.BOLD, MUTED);
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        left.add(lbl, BorderLayout.NORTH);

        String[] cols = {"#", "Statut", "Immatricule", "Marque", "Propriétaire"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        placesTable = new JTable(tableModel);
        styleTable(placesTable);

        JScrollPane sp = new JScrollPane(placesTable);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.setBackground(CARD);
        sp.getViewport().setBackground(CARD);
        left.add(sp, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(0, 0));
        right.setBackground(BG);
        right.setBorder(new EmptyBorder(16, 8, 0, 16));

        JLabel lblLog = label("JOURNAL", 11, Font.BOLD, MUTED);
        lblLog.setBorder(new EmptyBorder(0, 0, 10, 0));
        right.add(lblLog, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(CARD);
        logArea.setForeground(TXT);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane spLog = new JScrollPane(logArea);
        spLog.setBorder(BorderFactory.createLineBorder(BORDER));
        right.add(spLog, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerLocation(520);
        split.setDividerSize(6);
        split.setBackground(BG);
        split.setBorder(null);
        return split;
    }


    private JPanel buildFooter() {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        f.setBackground(CARD);
        f.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));

        f.add(btn("➕  Faire entrer une voiture", ACCENT,    BG,  e -> dialogEntrer()));
        f.add(btn("➖  Faire sortir une voiture", DANGER,    BG,  e -> dialogSortir()));
        f.add(btn("🧹  Vider le journal",         MUTED,     CARD, e -> logArea.setText("")));
        f.add(btn("⚙  Config parking",            ACCENT2,   BG,  e -> dialogConfig()));

        return f;
    }

    private void dialogEntrer() {
        JDialog d = dialog("Faire entrer une voiture");

        JTextField fImmat = field("AB-123-CD");
        JTextField fMarque = field("Renault");
        JTextField fProp   = field("Ahmed Benali");
        JCheckBox cbThread = new JCheckBox("Sortie automatique (thread)");
        cbThread.setBackground(CARD);
        cbThread.setForeground(TXT);
        cbThread.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel form = formPanel(new String[]{"Immatricule", "Marque", "Propriétaire"},
                new JComponent[]{fImmat, fMarque, fProp});
        form.add(Box.createVerticalStrut(8));
        form.add(cbThread);

        JButton ok = btn("Confirmer l'entrée", ACCENT, BG, null);
        ok.addActionListener(e -> {
            String im = fImmat.getText().trim();
            String ma = fMarque.getText().trim();
            String pr = fProp.getText().trim();
            if (im.isEmpty() || ma.isEmpty() || pr.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Tous les champs sont requis.");
                return;
            }
            voiture v = new voiture(im, ma, pr);
            if (cbThread.isSelected()) {
                Thread t = new Thread(new mythread(park, v));
                t.setDaemon(true);
                t.start();
            } else {
                park.bonjourvoiture(v);
            }
            rafraichir();
            d.dispose();
        });

        d.add(form, BorderLayout.CENTER);
        d.add(centered(ok), BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void dialogSortir() {
        JDialog d = dialog("Faire sortir une voiture");
        JTextField fNmr = field("0");

        JPanel form = formPanel(new String[]{"Numéro de place"},
                new JComponent[]{fNmr});

        JButton ok = btn("Confirmer la sortie", DANGER, BG, null);
        ok.addActionListener(e -> {
            try {
                int nmr = Integer.parseInt(fNmr.getText().trim());
                park.baybayvoiture(nmr);
                rafraichir();
                d.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "Entrez un numéro valide.");
            }
        });

        d.add(form, BorderLayout.CENTER);
        d.add(centered(ok), BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void dialogConfig() {
        JDialog d = dialog("Configuration du parking");
        JTextField fNom   = field(park.getNbr_places_max() + "");
        JTextField fPrix  = field(park.prixheure + "");

        JPanel form = formPanel(new String[]{"Nombre de places", "Prix / heure (DH)"},
                new JComponent[]{fNom, fPrix});

        JButton ok = btn("Recréer le parking", ACCENT2, BG, null);
        ok.addActionListener(e -> {
            try {
                int places = Integer.parseInt(fNom.getText().trim());
                double prix = Double.parseDouble(fPrix.getText().trim());
                park = new parking("Parking Central", places, prix);
                rafraichir();
                d.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Valeurs invalides.");
            }
        });

        d.add(form, BorderLayout.CENTER);
        d.add(centered(ok), BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }


    private void rafraichir() {
        SwingUtilities.invokeLater(() -> {
            // header stats
            lblPlacesMax.setText("Max: " + park.getNbr_places_max());
            lblPlacesDispo.setText("Dispo: " + park.getNbr_places_dispo());
            lblListeAttente.setText("File: " + park.liste_attente.size());

            // table
            tableModel.setRowCount(0);
            for (int i = 0; i < park.places.size(); i++) {
                boolean vide = park.places.get(i).vide;
                voiture v = park.voitures_presentes.get(i);
                if (vide) {
                    tableModel.addRow(new Object[]{i, "🟢 Libre", "—", "—", "—"});
                } else {
                    String immat = v != null ? v.getimmatricule() : "?";
                    String marque = v != null ? v.getMarque() : "?";
                    String prop = v != null ? v.getProprietere() : "?";
                    tableModel.addRow(new Object[]{i, "🔴 Occupée", immat, marque, prop});
                }
            }
        });
    }


    private void redirectSystemOut() {
        System.setOut(new java.io.PrintStream(System.out) {
            public void println(String x) {
                super.println(x);
                appendLog(x);
            }
            public void print(String x) {
                super.print(x);
            }
        });
    }

    private void appendLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            String ts = java.time.LocalTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.append("[" + ts + "] " + msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private JLabel label(String txt, int size, int style, Color c) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(c);
        return l;
    }

    private JButton btn(String txt, Color bg, Color fg, ActionListener al) {
        JButton b = new JButton(txt);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(9, 20, 9, 20));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        if (al != null) b.addActionListener(al);
        b.addMouseListener(new MouseAdapter() {
            Color orig = bg;
            public void mouseEntered(MouseEvent e) { b.setBackground(orig.brighter()); }
            public void mouseExited (MouseEvent e) { b.setBackground(orig); }
        });
        return b;
    }

    private JTextField field(String placeholder) {
        JTextField f = new JTextField(placeholder, 18);
        f.setBackground(new Color(35, 40, 54));
        f.setForeground(TXT);
        f.setCaretColor(ACCENT);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(6, 10, 6, 10)));
        return f;
    }

    private JDialog dialog(String titre) {
        JDialog d = new JDialog(this, titre, true);
        d.getContentPane().setBackground(CARD);
        d.setLayout(new BorderLayout(0, 16));
        ((JPanel) d.getContentPane()).setBorder(new EmptyBorder(20, 24, 20, 24));
        return d;
    }

    private JPanel formPanel(String[] labels, JComponent[] fields) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD);
        for (int i = 0; i < labels.length; i++) {
            JLabel l = label(labels[i], 12, Font.PLAIN, TXT2);
            l.setBorder(new EmptyBorder(8, 0, 4, 0));
            l.setAlignmentX(0f);
            fields[i].setAlignmentX(0f);
            if (fields[i] instanceof JTextField)
                ((JTextField) fields[i]).setMaximumSize(
                        new Dimension(Integer.MAX_VALUE, 36));
            p.add(l);
            p.add(fields[i]);
        }
        return p;
    }

    private JPanel centered(JButton b) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        p.setBackground(CARD);
        p.add(b);
        return p;
    }

    private void styleTable(JTable t) {
        t.setBackground(CARD);
        t.setForeground(TXT);
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t.setRowHeight(32);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(50, 60, 80));
        t.setSelectionForeground(TXT);
        t.getTableHeader().setBackground(new Color(30, 35, 48));
        t.getTableHeader().setForeground(TXT2);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        t.getTableHeader().setBorder(BorderFactory.createLineBorder(BORDER));

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable table, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(table, val, sel, focus, row, col);
                setOpaque(true);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (!sel) {
                    setBackground(row % 2 == 0 ? CARD : new Color(28, 33, 45));
                    String statut = (String) table.getValueAt(row, 1);
                    if (col == 1) {
                        setForeground(statut.contains("Libre") ? ACCENT : DANGER);
                    } else {
                        setForeground(TXT);
                    }
                }
                return this;
            }
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(parkingGUI::new);
    }
}