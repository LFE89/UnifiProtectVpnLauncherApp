# UnifiProtectVpnLauncherApp
Simple proof of concept, to demonstrate a way to bypass Unifi Protect VPN restrictions.

## License
This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.
For full license text, see http://www.gnu.org/licenses.

## General

The PoC does only work in combination with the Device App https://github.com/LFE89/UnifiProtectVpnDeviceApp The following sequence diagramm illustrates the work.

![Alt text](UBNT_VPN_SEQUENCE_D.png?raw=true "D.")

## Usage
1. Get discovery payload of your devices (e.g. by make use of client.js of https://github.com/bahamas10/unifi-proxy)
2. Install app on Unifi CK Gen 2 appliance (activate SSH).
3. Start UnifiProtectVpnDeviceApp with hex representation of discovery payload of step 1.
4. Start UnifiProtectVpnLauncherApp (https://github.com/LFE89/UnifiProtectVpnLauncherApp)
### Help
There are just two settings needed:
- IP address of Unifi Cloud Key Gen 2 (local network)
- IP address of VPN connection (Android device)

<img src="android_app_unifi_protect_launcher.jpg?raw=true" data-canonical-src="android_app_unifi_protect_launcher.jpg?raw=true" width="250" />

### Demo
<img src="poc_unifi_protect_vpn.gif?raw=true" data-canonical-src="poc_unifi_protect_vpn.gif?raw=true" width="250" />
