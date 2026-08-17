package Solicitacao.Material.Hhtec.Exceptions;

public class SemPermissaoException extends RuntimeException {
    public SemPermissaoException(String message) {
        super(message);
    }
}
