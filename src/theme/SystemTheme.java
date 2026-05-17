package theme;

import java.awt.Color;
import java.awt.Font;

public class SystemTheme {
    public static final Color PRIMARY_COLOR = Color.decode("#0C5868");
    public static final Color ACCENT_COLOR = Color.decode("#F5F5F5");
    public static final Color DANGER_COLOR = Color.decode("#ef4444");
    
    public static final Color CARD_BG = Color.decode("#F8FAFC");
    public static final Color FIELD_BG = Color.decode("#E2E8F0");
    public static final Color BORDER_COLOR = Color.decode("#CBD5E1");

    
    public static final Color TEXT_COLOR = Color.decode("#FFFFFF");
    public static final Color TEXT_MAIN = Color.decode("#0F172A");
    public static final Color TEXT_MUTED = Color.decode("#475569");
    public static final Color TEXT_INDICATOR = Color.decode("#2ecae6");
    
    public static final Color BTN_YES = Color.decode("#238735");
    public static final Color BTN_NO = Color.decode("#b02f0b");
    public static final Color BTN_REFRESH = Color.decode("#09619c");
    public static final Color BTN_DARK = Color.decode("#020203");
    
    public static final Color BADGE_VL = Color.decode("#207309");
    public static final Color BADGE_SL = Color.decode("#7a0956");
    public static final Color BADGE_TEXT = Color.decode("#e0f0f5");
    
    public static final Color ROW_SELECTED = Color.decode("#1E3A5F");

    
    private static final String FONT_FAMILY = "Segoe UI";
    
    public static final Font NORMAL_TEXT = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font BOLD_TEXT = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font SMALL_TEXT_PLAIN = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font SMALL_TEXT_BOLD = new Font(FONT_FAMILY, Font.BOLD, 12);
    public static final Font LARGE_TEXT_PLAIN = new Font(FONT_FAMILY, Font.PLAIN, 18);
    public static final Font LARGE_TEXT_BOLD = new Font(FONT_FAMILY, Font.BOLD, 18); 
    public static final Font BIG_TEXT_PLAIN = new Font(FONT_FAMILY, Font.PLAIN, 24);
    public static final Font BIG_TEXT_BOLD = new Font(FONT_FAMILY, Font.BOLD, 24);
    
    public static final Font HEADER_TEXT = new Font(FONT_FAMILY, Font.BOLD, 30);
}
