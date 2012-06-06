
private float truncateFloat( float flo ){
	if( flo < 1 ) {
		return new Float( String.format( "%.3f", new Object[] { flo } ) );
	} else {
		return new Float( String.format( "%.4G", new Object[] { flo } ) );
	}
}

private class Rate {
	public float rate;
	public String units;
	
	public Rate( String rate_string ) {
		rate = new Float( rate_string.substring(0,5) );
		units = rate_string.substring(5,7);
	}
	
	public Rate( float frate, String sunits ) {
		rate = frate;
		units = sunits;
	}
	
	public Boolean equals( Rate otherRate ) {
		return ( ( rate == otherRate.rate ) && units.equals( otherRate.units ) );
	}
	
	public String toString() {
		if ( rate < 1 ) {
			return String.format( "%.3G%s", new Object[] { rate, units } );
		} else {
			return String.format( "%.4G%s", new Object[] { rate, units } );
		}
	}
}

class SyringePump {
	
	private int address;
	private String port;
	
	static private String comTer = "\r";
	static private String ansTer = "\003";
	
	public SyringePump( int startAddress, String startPort, int syringeVolume ) throws Exception {
		address = startAddress;
		port = startPort;		
		if( !init() ) {
			throw new Exception( "Initialisation failed" );
		}
		switch( syringeVolume ) {
			case 1:
				setDiameter( 4.699 );
				break;
			case 3:
				setDiameter( 8.585 );
				break;
			case 5:
				setDiameter( 11.99 );
				break;
			case 10:
				setDiameter( 14.43 );
				break;
			case 20:
				setDiameter( 19.05 );
				break;
			case 30:
				setDiameter( 21.59 );
				break;
			case 60:
				setDiameter( 26.59 );
				break;
			default:
				throw new Exception( "Unsupported syringe size. Must be one of 1, 3, 5, 10, 20, 30 or 60" );
		}
	}
	
	
	private Boolean init() {
		try {
			sendCommand( "" );
		} catch( Exception e ) {
			print ( "Could not connect to syringe pump with address " + address + " on port " + port );
			return false;
		}
		return true;
	}
	
	int getAddress() {
		return address;
	}
	
	String getPort() {
		return port;
	}
	
	String sendCommand( String command ) {
		command = address + " " + command;
		mmc.setSerialPortCommand( port, command, comTer );
		answer = mmc.getSerialPortAnswer( port, ansTer );
		String code = answer.substring(3,4);
		String message = answer.substring(4);
		return message;
	}
	
	String getDirection() {
		return sendCommand( "DIR" );
	}
	
	void setDirection( String direction ) {
		switch( direction ) {
			case "INF":	
			case "Infuse":	sendCommand( "DIR INF" );
							break;
			case "WDR":
			case "Withdraw": 	sendCommand( "DIR WDR" );
								break;
			case "REV":
			case "Reverse":
			case "Toggle":	sendCommand( "DIR REV" );
							break;
			default:	print( "Invalid direction" );
		}
	}
	
	void toggleDirection() {
		sendCommand( "DIR REV" );
	}
	
	float getDiameter() {
		Float diameter = new Float( sendCommand( "DIA" ) );
		return diameter;
	}
	
	void setDiameter( double ddiameter ) {
		float diameter = ddiameter;
		setDiameter( diameter );
	}
	
	void setDiameter( float diameter ) {
		sendCommand( "DIA " + diameter );
		if( getDiameter() != diameter ) {
			print( "Could not set diameter " + diameter );
		}
	}
	
	Rate getRate() {
		return new Rate( sendCommand( "RAT" ) );
	}
	
	void setRate( double drate, String units ) {
		float rate = new Float( drate );
		setRate( rate, units );
	}
	
	void setRate( float rate, String units ) {
		Rate newRate = new Rate( rate, units );
		sendCommand( "RAT " + newRate.toString() );
		if( !newRate.equals( getRate() ) ) {
			print( "Could not set rate " + newRate.toString() );
		}
	}
	
	float getVolume() {
		float volume = new Float( sendCommand( "VOL" ).substring( 0,5 ) );
		return volume;
	}
	
	void setVolume( double dvolume ) {
		float volume = dvolume;
		setVolume( volume );
	}
	
	void setVolume( float volume ) {
		sendCommand( "VOL " + volume );
		print( getVolume() );
		if( getVolume() != volume ) {
			print( "Could not set volume " + volume );
		}
	}
	
	void infuseTime( double dtime, double drate, String units ) {
		float time = dtime;
		float rate = drate;
		infuseTime( time, rate, units );
	}
	
	void infuseTime( float time, float rate, String units ) {
		float volume;
		switch( units ) {
			case "MM":
				volume = time * rate / 60;
				break;
			case "MH":
				volume = time * rate / 3600;
				break;
			case "UM":
				volume = time * rate / 60000;
				break;
			case "UH":
				volume = time * rate / 3600000;
				break;
			default:
				volume = 0.0001;
				break;
		}
		print( volume );
		volume = truncateFloat( volume );
		print( volume );
		infuseVolume( volume, rate, units );
	}
	
	void infuseVolume( double dvolume, double drate, String units ) {
		float volume = dvolume;
		float rate = drate;
		infuseVolume( volume, rate, units );
	}
	
	void infuseVolume( float volume, float rate, String units  ) {
		sendCommand( "FUN RAT" );
		setDirection( "INF" );
		setVolume( volume );
		setRate( rate, units );
		sendCommand( "RUN" );
	}
	
	void infuse( double drate, String units ) {
		float rate = drate;
		infuse( rate, units );
	}
	
	void infuse( float rate, String units ) {
		sendCommand( "FUN RAT" );
		setDirection( "INF" );
		setRate( rate, units );
		sendCommand( "RUN" );
	}
	
	void withdrawVolume( double dvolume, double drate, String units ) {
		float volume = dvolume;
		float rate = drate;
		withdrawVolume( volume, rate, units );
	}
	
	void withdrawVolume( float volume, float rate, String units  ) {
		sendCommand( "FUN RAT" );
		setDirection( "WDR" );
		setVolume( volume );
		setRate( rate, units );
		sendCommand( "RUN" );
	}
	
	void withdraw( double drate, String units ) {
		float rate = drate;
		withdraw( rate, units );
	}
	
	void withdraw( float rate, String units ) {
		sendCommand( "FUN RAT" );
		setDirection( "WDR" );
		setRate( rate, units );
		sendCommand( "RUN" );
	}
	
	String run () {
		return sendCommand( "RUN" );
	};
	
	String stop() {
		return sendCommand( "STP" );
	}
	
	void beep() {
		sendCommand( "BUZ 1 1" );
	}
	
	void beep( int n ) {
		sendCommand( "BUZ 1 " + n );
	}
}