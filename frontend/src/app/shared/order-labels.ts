const paymentMethods: Record<string, string> = {
  PIX: 'PIX',
  BOLETO: 'Boleto',
  CREDIT_CARD: 'Cartão de crédito',
  LEGACY: 'Pedido antigo',
};

const orderStatuses: Record<string, string> = {
  CONFIRMED: 'Confirmado',
  PROCESSING: 'Em preparação',
  DELIVERED: 'Entregue',
};

const paymentStatuses: Record<string, string> = {
  PENDING: 'Aguardando pagamento',
  SIMULATED_APPROVED: 'Aprovado',
};

export const orderLabels = {
  method: (value: string) => paymentMethods[value] ?? value,
  status: (value: string) => orderStatuses[value] ?? value,
  payment: (value: string) => paymentStatuses[value] ?? value,
};
