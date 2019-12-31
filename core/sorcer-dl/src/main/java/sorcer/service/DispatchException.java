/*
 * Distribution Statement
 * 
 * This computer software has been developed under sponsorship of the United States Air Force Research Lab. Any further
 * distribution or use by anyone or any data contained therein, unless otherwise specifically provided for,
 * is prohibited without the written approval of AFRL/RQVC-MSTC, 2210 8th Street Bldg 146, Room 218, WPAFB, OH  45433
 * 
 * Disclaimer
 * 
 * This material was prepared as an account of work sponsored by an agency of the United States Government. Neither
 * the United States Government nor the United States Air Force, nor any of their employees, makes any warranty,
 * express or implied, or assumes any legal liability or responsibility for the accuracy, completeness, or usefulness
 * of any information, apparatus, product, or process disclosed, or represents that its use would not infringe privately
 * owned rights.
 */
package sorcer.service;

public class DispatchException extends Exception {

	private static final long serialVersionUID = 8010780565491710047L;
	
	private Context context;
	
	public DispatchException() {
	}

	public DispatchException(String msg) {
		super(msg);
	}
	
	public DispatchException(Context context) {
		this.context = context;
	}

	public DispatchException(String msg, Context context) {
		super(msg);
		this.context = context;
	}
	
	public DispatchException(Exception exception) {
		super(exception);
	}

	public DispatchException(String msg, Exception e) {
		super(msg, e);
		
	}
	
	public DispatchException(String msg, Context context, Exception e) {
		super(msg, e);
		this.context = context;
	}
	
	public Context getContext() {
		return context;
	}
}
