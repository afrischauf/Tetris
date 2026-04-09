package config;

import lombok.SneakyThrows;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.*;

/**
 * Class for writing maps of data into a text file
 */
public class LdataWriter {

    /**
     * Default assignment character for key-value pairs.
     */
    public static final String assignmentChar = "=";

    /**
     * Alternative assignment character for key-value pairs.
     */
    public static final String altAssignmentChar = ":";


    /**
     * Writes the given map of data to the specified writer.
     *
     * @param _altAssign Flag indicating whether to use the alternative assignment character.
     * @param _data      The map of data to be written.
     * @param _out       The writer to which the data should be written.
     */
    public static void writeTo(boolean _altAssign, Map<String, Object> _data, Writer _out) {
        writeTo(_altAssign, _data, _out, false);
    }


    /**
     * Writes the given map of data to the specified writer with an option to automatically close the writer.
     *
     * @param _altAssign Flag indicating whether to use the alternative assignment character.
     * @param _data      The map of data to be written.
     * @param _out       The writer to which the data should be written.
     * @param _autoclose Flag indicating whether to automatically close the writer after writing.
     */
    public static void writeTo(boolean _altAssign, Map<String, Object> _data, Writer _out, boolean _autoclose) {
        try {
            writeTo(_altAssign, 0, _data, _out);
        } finally {
            if (_autoclose) IOUtil.close(_out);
        }
    }


    /**
     * Writes the map of data to the specified writer at a specific indentation level.
     *
     * @param _altAssign Flag indicating whether to use the alternative assignment character.
     * @param _level     The indentation level.
     * @param _data      The map of data to be written.
     * @param _out       The writer to which the data should be written.
     */
    @SneakyThrows
    public static void writeTo(boolean _altAssign, int _level, Map<String, Object> _data, Writer _out) {
        _out.write(" {");

        List<String> _keys = new Vector(_data.keySet());
        _keys.sort((x, y) -> x.compareToIgnoreCase(y));

        for (String _key : _keys) {
            _out.write(MessageFormat.format("\n{0}\"{1}\" {2} ", StringUtils.repeat(" ", (_level == 0 ? 0 : _level + 1)), _key, _altAssign ? altAssignmentChar : assignmentChar));
            writeTo(_altAssign, _level + 2, _data.get(_key), _out);
        }

        _out.write("\n" + StringUtils.repeat(" ", _level) + "}");

        _out.flush();
    }

    /**
     * Writes the specified data to the provided writer based on its type.
     *
     * @param _altAssign Flag indicating whether to use the alternative assignment character.
     * @param _level     The indentation level for formatting.
     * @param _data      The data to be written.
     * @param _out       The writer to which the data should be written.
     */
    @SneakyThrows
    public static void writeTo(boolean _altAssign, int _level, Object _data, Writer _out) {
        if (_data == null) {
            _out.write("null\n");
        } else if (_data instanceof Map) {
            writeTo(_altAssign, _level, (Map) _data, _out);
        } else if (_data instanceof List) {
            writeTo(_altAssign, _level, (List) _data, _out);
        } else if (_data instanceof String) {
            writeTo(_level, (String) _data, _out);
        } else if (_data instanceof Integer) {
            writeTo(_level, (Integer) _data, _out);
        } else if (_data instanceof Long) {
            writeTo(_level, (Long) _data, _out);
        } else if (_data instanceof Boolean) {
            writeTo(_level, (Boolean) _data, _out);
        } else if (_data instanceof Double) {
            writeTo(_level, (Double) _data, _out);
        } else if (_data instanceof Float) {
            writeTo(_level, (Float) _data, _out);
        } else if (_data.getClass().isArray()) {
            writeTo(_altAssign, _level, Arrays.asList((Object[]) _data), _out);
        } else {
            writeTo(_level, _data.toString(), _out);
        }
    }

    /**
     * Writes the elements of a list to the provided writer.
     *
     * @param _altAssign Flag indicating whether to use the alternative assignment character.
     * @param _level     The indentation level for formatting.
     * @param _data      The list of data to be written.
     * @param _out       The writer to which the data should be written.
     */
    @SneakyThrows
    public static void writeTo(boolean _altAssign, int _level, List _data, Writer _out) {
        _out.write(" [ ");
        for (Object _o : _data) {
            writeTo(_altAssign, _level + 1, _o, _out);
        }
        _out.write(" ] ");
        _out.flush();
    }


    /**
     * Writes an integer value to the provided writer.
     *
     * @param _level The indentation level for formatting.
     * @param _data  The integer value to be written.
     * @param _out   The writer to which the data should be written.
     */
    @SneakyThrows
    public static void writeTo(int _level, Integer _data, Writer _out) {
        _out.write(" " + (_data) + " ");
    }

    /**
     * Writes a long value to the provided writer.
     *
     * @param _level The indentation level for formatting.
     * @param _data  The long value to be written.
     * @param _out   The writer to which the data should be written.
     */
    @SneakyThrows
    public static void writeTo(int _level, Long _data, Writer _out) {
        _out.write(" " + (_data) + " ");
    }

    /**
     * Writes a boolean value to the provided writer.
     *
     * @param _level The indentation level for formatting.
     * @param _data  The boolean value to be written.
     * @param _out   The writer to which the data should be written.
     */
    @SneakyThrows
    public static void writeTo(int _level, Boolean _data, Writer _out) {
        _out.write(_data ? " true " : " false ");
    }

    /**
     * Writes a double value to the provided writer.
     *
     * @param _level The indentation level for formatting.
     * @param _data  The double value to be written.
     * @param _out   The writer to which the data should be written.
     */
    @SneakyThrows
    public static void writeTo(int _level, Double _data, Writer _out) {
        _out.write(String.format(" %.9f ", _data.doubleValue()));
    }

    /**
     * Writes a float value to the provided writer.
     *
     * @param _level The indentation level for formatting.
     * @param _data  The float value to be written.
     * @param _out   The writer to which the data should be written.
     */
    @SneakyThrows
    public static void writeTo(int _level, Float _data, Writer _out) {
        _out.write(String.format(" %.6f ", _data.floatValue()));
    }

    /**
     * Writes a string value to the provided writer, handling special characters and escaping as needed.
     *
     * @param _level The indentation level for formatting.
     * @param _data  The string value to be written.
     * @param _out   The writer to which the data should be written.
     */
    @SneakyThrows
    public static void writeTo(int _level, String _data, Writer _out) {
        if (_data.indexOf('\n') >= 0 || _data.indexOf('\r') >= 0 || _data.indexOf('\t') >= 0 || _data.indexOf('"') >= 0) {
            _out.write("\n\"\"\"");
            writeToEscaped(true, _data, _out);
            _out.write("\"\"\" ");
        } else {
            _out.write(" \"");
            writeToEscaped(false, _data, _out);
            _out.write("\" ");
        }
        _out.flush();
    }

    /**
     * Writes a string value to the provided writer with proper escaping based on the given parameters.
     *
     * @param _tripleQuoted Flag indicating whether triple quotes are used.
     * @param _data         The string value to be written.
     * @param _out          The writer to which the data should be written.
     */
    @SneakyThrows
    public static void writeToEscaped(boolean _tripleQuoted, String _data, Writer _out) {
        for (int _i = 0; _i < _data.length(); _i++) {
            char _c = _data.charAt(_i);
            if (_tripleQuoted && _c < 0x20) {
                _out.write(Character.toString(_c));
            } else if (!_tripleQuoted && _c < 0x20) {
                _out.write(String.format("\\x%02X", (int) _c));
            }
            if (_c > 0xff) {
                _out.write(String.format("\\u%04X", (int) _c));
            } else if (_c == '\\') {
                _out.write("\\\\");
            } else {
                _out.write(Character.toString(_c));
            }
        }
    }
}
