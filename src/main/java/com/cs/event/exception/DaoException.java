package com.cs.event.exception;

/**
 * @author dmuni
 *
 */
public class DaoException extends RuntimeException {

	private static final long serialVersionUID = 1615705016263493108L;

	/**
	 * @param errorMessage
	 */
	public DaoException(String errorMessage) {
        super(errorMessage);
    }
}
