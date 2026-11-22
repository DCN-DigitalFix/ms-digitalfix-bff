package cl.duoc.ms_digitalfix_bff.exception;

/** El repuesto existe pero no tiene stock suficiente para la cantidad pedida. */
public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException(String message) {
        super(message);
    }
}
