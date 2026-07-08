// Use this from your Patient screen / Appointment screen when receptionist clicks Send SMS.

export async function createSmsJobExample(prisma: any, input: {
  patientId?: string;
  phoneNumber: string;
  messageBody: string;
  createdByUserId?: string;
}) {
  const phone = normalizePakistanPhone(input.phoneNumber);
  if (!phone) throw new Error('Invalid Pakistan mobile number');
  if (!input.messageBody || input.messageBody.length > 612) throw new Error('Message is empty or too long');

  return prisma.smsOutbox.create({
    data: {
      patientId: input.patientId,
      phoneNumber: phone,
      messageBody: input.messageBody,
      createdByUserId: input.createdByUserId,
      provider: 'ANDROID_PHONE',
      status: 'PENDING',
    },
  });
}

function normalizePakistanPhone(raw: string) {
  const digits = raw.replace(/[^0-9+]/g, '');
  if (digits.startsWith('+92') && digits.length === 13) return digits;
  if (digits.startsWith('92') && digits.length === 12) return '+' + digits;
  if (digits.startsWith('03') && digits.length === 11) return '+92' + digits.slice(1);
  return null;
}
