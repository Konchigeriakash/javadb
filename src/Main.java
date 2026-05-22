public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                DatabaseSetup.setup();
                new InventoryDashboard().setVisible(true);
            } catch (Exception ex) {
                DialogHelper.showError(null, ex);
            }
        });
    }
}
