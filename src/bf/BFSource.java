package bf;

public class BFSource {
    private boolean textUpdated = false;
    private String text;
    private StringBuilder buffer = new StringBuilder();

    public BFSource() {
        this("");
    }

    public BFSource(String text) {
        buffer.append(text);
    }

    public BFSource append(String text) {
        buffer.append(text);
        textUpdated = false;
        return this;
    }

    public BFSource append(BFSource source) {
        buffer.append(source.buffer);
        textUpdated = false;
        return this;
    }

    public String getText() {
        if (!textUpdated) updateText();
        return text;
    }

    @Override
    public String toString() {
        return getText();
    }

    private void updateText() {
        text = buffer.toString();
        textUpdated = true;
    }

}
