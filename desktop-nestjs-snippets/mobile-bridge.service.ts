import { Injectable, UnauthorizedException, BadRequestException } from '@nestjs/common';
import { createHash, randomBytes } from 'crypto';
// Replace this with your real PrismaService import.
// import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class MobileBridgeService {
  // constructor(private readonly prisma: PrismaService) {}

  private hash(value: string) {
    return createHash('sha256').update(value).digest('hex');
  }

  async pairDevice(pairingToken: string, deviceName: string) {
    // MVP: read token from in-memory/session setting created when desktop shows QR.
    // Production: token must expire in 2 minutes and be single-use.
    const activePairingToken = process.env.SMS_BRIDGE_PAIRING_TOKEN || '123456';
    if (!pairingToken || pairingToken !== activePairingToken) {
      throw new UnauthorizedException('Invalid pairing token');
    }

    const deviceSecret = randomBytes(32).toString('hex');

    // Example Prisma create:
    // const device = await this.prisma.smsBridgeDevice.create({
    //   data: {
    //     deviceName,
    //     deviceSecretHash: this.hash(deviceSecret),
    //     status: 'ONLINE',
    //     lastSeenAt: new Date(),
    //   },
    // });

    const device = { id: 'replace-with-prisma-device-id', deviceName };

    return {
      deviceId: device.id,
      deviceSecret,
      clinicName: 'Farooq EyeCare Hospital',
    };
  }

  async verifyDeviceOrThrow(deviceId: string, deviceSecret: string) {
    if (!deviceId || !deviceSecret) throw new UnauthorizedException('Missing device credentials');

    // const device = await this.prisma.smsBridgeDevice.findUnique({ where: { id: deviceId } });
    // if (!device || device.status === 'DISABLED') throw new UnauthorizedException('Device not allowed');
    // if (device.deviceSecretHash !== this.hash(deviceSecret)) throw new UnauthorizedException('Invalid device secret');

    // For MVP snippet only. Remove this when Prisma is connected.
    if (deviceId !== 'replace-with-prisma-device-id') throw new UnauthorizedException('Device not allowed');
    return true;
  }

  async heartbeat(deviceId: string, body: { selectedSubId?: number; selectedSimLabel?: string; status?: string }) {
    // await this.prisma.smsBridgeDevice.update({
    //   where: { id: deviceId },
    //   data: {
    //     selectedSubId: body.selectedSubId,
    //     selectedSimLabel: body.selectedSimLabel,
    //     status: 'ONLINE',
    //     lastSeenAt: new Date(),
    //   },
    // });
    return { ok: true };
  }

  async getPendingSms(deviceId: string) {
    // IMPORTANT: limit to avoid accidental bulk sending.
    // Mark items as SENDING or LOCKED before returning in production to avoid duplicates.
    // return this.prisma.smsOutbox.findMany({
    //   where: { status: 'PENDING', provider: 'ANDROID_PHONE' },
    //   take: 3,
    //   orderBy: { createdAt: 'asc' },
    //   select: { id: true, phoneNumber: true, messageBody: true },
    // });

    return [];
  }

  async updateSmsStatus(deviceId: string, body: { smsId: string; status: string; errorMessage?: string }) {
    const allowed = ['SENDING', 'SENT', 'DELIVERED', 'FAILED'];
    if (!allowed.includes(body.status)) throw new BadRequestException('Invalid status');

    // await this.prisma.smsOutbox.update({
    //   where: { id: body.smsId },
    //   data: {
    //     status: body.status,
    //     deviceId,
    //     errorMessage: body.errorMessage,
    //     sentAt: body.status === 'SENT' ? new Date() : undefined,
    //     deliveredAt: body.status === 'DELIVERED' ? new Date() : undefined,
    //   },
    // });
    // await this.prisma.smsAuditLog.create({ data: { smsId: body.smsId, deviceId, action: body.status, message: body.errorMessage } });
    return { ok: true };
  }
}
