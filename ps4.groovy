/*
 * PlayStation 4 driver
 *
 * For detecting status of your PlayStation 4.
 * 
 */

metadata {
    definition(name: "PlayStation 4", namespace: "community", author: "cometfish") {
        capability "Actuator"
        capability "Switch"
		
		attribute "switch", "enum", ["off", "on"]
        attribute "power", "enum", ["off", "on", "standby"]
		
		command "on" 
		command "off"
		
		command "refresh"
    }
}

preferences {
    section("URIs") {
        input "ipAddress", "text", title: "IP Address", required: true
        input "pollInterval", "text", title: "Poll interval (minutes, or 0 for off)", required: true, defaultValue: "0"
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}

def updated() {
    log.info "Settings updated"
    
	unschedule()
    if(settings?.pollInterval>0 && settings?.pollInterval<=60){
	    def pollIntervalCmd = (settings?.pollInterval)
        Random rand = new Random(now())
    	def randomSeconds = rand.nextInt(60)
        def sched = "${randomSeconds} */${pollIntervalCmd} * * * ?"
        schedule("${sched}", "refresh")
    }
}
def refresh() {
    if (state.stillWaiting)
    {
        log.warn 'already checking status - please wait for response before checking again';
        return;
    }
	sendMsg("SRCH * HTTP/1.1\r\ndevice - discovery - protocol - version:00020020\r\n", refreshCallback);
}
def noResponse() {
    if (state.stillWaiting)
    {
        //off
        if (device.currentValue("power")!="off" && !state.recheckOff) {
            //going from on to standby mode, or standby mode to on, the PS has about a 30s wait where it doesn't respond at all (looking like it's off)
            //so try again in 35s - if it's still off, then it's definitely off
            if (logEnable)
                log.info "off - will confirm in 35s since it was previously on/standby";
            state.stillWaiting = false;
            state.recheckOff = true;
            runIn(35, refresh);
            return;
        }
        if (logEnable)
            log.info "off";
        sendEvent(name: "switch", value: "off", isStateChange: true);
        sendEvent(name: "power", value: "off", isStateChange: true);
        state.stillWaiting = false;
        state.recheckOff = false;
    }
}
def refreshCallback(String response) {
    state.stillWaiting = false;
    state.recheckOff = false;
    httpResponse = new String(hubitat.helper.HexUtils.hexStringToByteArray(parseLanMessage(response).payload));
    if (httpResponse.toUpperCase().contains("HTTP/1.1 200")) {
        //on
        if (logEnable)
            log.info "on";
        sendEvent(name: "switch", value: "on", isStateChange: true);
        sendEvent(name: "power", value: "on", isStateChange: true);
    } else if (httpResponse.toUpperCase().contains("HTTP/1.1 620")) {
        //standby
        if (logEnable)
            log.info "standby mode";
        sendEvent(name: "switch", value: "off", isStateChange: true);
        sendEvent(name: "power", value: "standby", isStateChange: true);
    } else {
        //off
        if (logEnable)
            log.info "off";
        sendEvent(name: "switch", value: "off", isStateChange: true);
        sendEvent(name: "power", value: "off", isStateChange: true);
    }
}
def on() {
	log.warn 'Changing PlayStation 4 status is currently unsupported.';
}
def off() {
	log.warn 'Changing PlayStation 4 status is currently unsupported.';
}
def parse(String description) {
}
def sendMsg(msg, msgResponse) {
	if (logEnable)
        log.info 'sending:'+msg;
	
    state.stillWaiting = true;
    runIn(7, noResponse);
	def myHubAction = new hubitat.device.HubAction(msg, 
                           hubitat.device.Protocol.LAN, 
                           [type: hubitat.device.HubAction.Type.LAN_TYPE_UDPCLIENT, 
                            destinationAddress: settings.ipAddress+":987",
							callback: msgResponse,
                            timeout: 5
							]) 
	sendHubCommand(myHubAction)
}
