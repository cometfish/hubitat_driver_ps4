# PlayStation 4 driver

Hubitat Driver for detecting status of your PlayStation 4.

1. Add `ps4.groovy` to your Hubitat as a new Driver (under `Drivers Code`)
2. Add a new device for your PlayStation 4 to your Hubitat, set device Type to your User driver of 'PlayStation 4'
3. Assign your PS4 a static IP, and enter the IP into the device's `IP Address` setting in Hubitat.
4. Pressing Refresh on the device in Hubitat should now load the current switch state (on/off). Enjoy :)

References:  
Protocol: https://psdevwiki.com/ps4/Online_Connections  
Working protocol examples: https://github.com/dhleong/ps4-waker , https://github.com/charliechancc/WebWOL
