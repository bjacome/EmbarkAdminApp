// (c) 2015 Western University (www.objectof.net)
// (c) 2015 IBM
// See License.txt in the distribution

package net.objectof.sql;

/**
 * A RuntimeException wrapper for SQLExceptions and other low-level execeptions
 * when working with SQL/JDBC.
 * 
 * @author jdh
 *
 */
public class SqlException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SqlException(String aMessage, Exception aCause) {
		super(aMessage, aCause);
	}

	public SqlException(String aMessage) {
		super(aMessage);
	}

	public SqlException(Exception aCause) {
		super(aCause);
	}
}
