package net.kraschitzer.intellij.plugin.sevenpace.model.enums;

public enum BranchCheckoutBehaviour {
    DIALOG("Show Dialog to start tracking"),
    AUTO_TRACK("Automatically start tracking"),
    OFF("Do nothing"),
    ;

    private String comboText;

    BranchCheckoutBehaviour(String comboText) {
        this.comboText = comboText;
    }

    @Override
    public String toString() {
        return comboText;
    }
}
