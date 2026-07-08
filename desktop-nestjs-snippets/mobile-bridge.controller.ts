import { Body, Controller, Get, Headers, Post, UnauthorizedException } from '@nestjs/common';
import { MobileBridgeService } from './mobile-bridge.service';

@Controller('mobile-bridge')
export class MobileBridgeController {
  constructor(private readonly bridge: MobileBridgeService) {}

  @Post('pair')
  async pair(@Body() body: { pairingToken: string; deviceName: string }) {
    return this.bridge.pairDevice(body.pairingToken, body.deviceName);
  }

  @Post('heartbeat')
  async heartbeat(
    @Headers('x-device-id') deviceId: string,
    @Headers('x-device-secret') deviceSecret: string,
    @Body() body: { selectedSubId?: number; selectedSimLabel?: string; status?: string },
  ) {
    await this.bridge.verifyDeviceOrThrow(deviceId, deviceSecret);
    return this.bridge.heartbeat(deviceId, body);
  }

  @Get('sms/pending')
  async pending(
    @Headers('x-device-id') deviceId: string,
    @Headers('x-device-secret') deviceSecret: string,
  ) {
    await this.bridge.verifyDeviceOrThrow(deviceId, deviceSecret);
    return this.bridge.getPendingSms(deviceId);
  }

  @Post('sms/status')
  async status(
    @Headers('x-device-id') deviceId: string,
    @Headers('x-device-secret') deviceSecret: string,
    @Body() body: { smsId: string; status: string; errorMessage?: string; androidTimeMillis?: number },
  ) {
    await this.bridge.verifyDeviceOrThrow(deviceId, deviceSecret);
    return this.bridge.updateSmsStatus(deviceId, body);
  }
}
