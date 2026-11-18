(() => {
 const track = document.getElementById('profile-review-track');
 if (!track) return;
 const controls = document.getElementById('review-controls');
 const prev = document.getElementById('reviews-prev'), next = document.getElementById('reviews-next');
 function update() {
  const max = track.scrollWidth - track.clientWidth;
  controls.hidden = max <= 1;
  prev.disabled = track.scrollLeft <= 1;
  next.disabled = track.scrollLeft >= max - 1;
 }
 function move(direction) {
  const card = track.querySelector('article');
  track.scrollBy({left: direction * (card.getBoundingClientRect().width + 20), behavior: matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth'});
 }
 prev.addEventListener('click', () => move(-1));
 next.addEventListener('click', () => move(1));
 track.addEventListener('scroll', update, {passive:true});
 new ResizeObserver(update).observe(track);
 update();
})();
