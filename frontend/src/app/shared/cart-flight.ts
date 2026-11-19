let nextFlightId = 0;

export function flyToCart(source: HTMLElement): Animation | undefined {
  const document = source.ownerDocument;
  const view = document.defaultView;
  if (!view || view.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

  const target = document.querySelector<HTMLElement>('[data-cart-flight-target]');
  if (!target) return;
  const start = source.getBoundingClientRect();
  const end = target.getBoundingClientRect();
  if (!start.width || !start.height || start.bottom <= 0 || start.top >= view.innerHeight
      || !end.width || end.bottom <= 0 || end.top >= view.innerHeight) return;

  const flight = source.cloneNode(true) as HTMLElement;
  const clipPath = flight.querySelector('clipPath[id]');
  const clippedImage = flight.querySelector('image[clip-path]');
  if (clipPath && clippedImage) {
    // Illustrations use a local SVG outline. The moving copy needs its own ID.
    clipPath.id = `cart-flight-outline-${nextFlightId++}`;
    clippedImage.setAttribute('clip-path', `url(#${clipPath.id})`);
  }
  flight.classList.add('cart-flight');
  flight.setAttribute('aria-hidden', 'true');
  flight.inert = true;
  Object.assign(flight.style, {
    left: `${start.left}px`,
    top: `${start.top}px`,
    width: `${start.width}px`,
    height: `${start.height}px`,
  });
  document.body.append(flight);

  const x = end.left + end.width / 2 - (start.left + start.width / 2);
  const y = end.top + end.height / 2 - (start.top + start.height / 2);
  const animation = flight.animate([
    { transform: 'none', opacity: 1 },
    { transform: `translate(${x * .39}px, ${y * .47 - 23}px) rotate(-8deg) scale(.66)`, opacity: .95, offset: .49 },
    { transform: `translate(${x}px, ${y}px) rotate(-14deg) scale(.08)`, opacity: .18 },
  ], { duration: 560, easing: 'cubic-bezier(.28, .15, .35, 1)', fill: 'forwards' });

  void animation.finished.then(() => {
    flight.remove();
    if (target.isConnected) {
      target.animate([
        { transform: 'scale(1)' },
        { transform: 'scale(1.3) rotate(-9deg)' },
        { transform: 'scale(1)' },
      ], { duration: 250, easing: 'ease-out' });
    }
  }, () => flight.remove());

  return animation;
}
