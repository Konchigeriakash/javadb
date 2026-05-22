import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.GraphicsEnvironment;

public class DialogHelper {
    private DialogHelper() {
    }

    public static void showError(Component parent, Exception ex) {
        ex.printStackTrace();
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        JOptionPane.showMessageDialog(parent, friendlyMessage(ex), "Inventory Management", JOptionPane.ERROR_MESSAGE);
    }

    private static String friendlyMessage(Exception ex) {
        if (ex instanceof IllegalArgumentException) {
            return ex.getMessage();
        }
        String message = ex.getMessage();
        return message == null || message.isBlank() ? "Something went wrong. Please check the entered data." : message;
    }
}
