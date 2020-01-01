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
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}

def updated() {
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
        if (logEnable)
            log.info "off";
        sendEvent(name: "switch", value: "off", isStateChange: true);
        sendEvent(name: "power", value: "off", isStateChange: true);
        state.stillWaiting = false;
    }
}
def refreshCallback(String response) {
    state.stillWaiting = false;
    httpResponse = new String(hubitat.helper.HexUtils.hexStringToByteArray(parseLanMessage(response).payload));
    if (httpResponse.toUpperCase().contains("HTTP/1.1 200")) {
        //powered on
        if (logEnable)
            log.info "powered on";
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
