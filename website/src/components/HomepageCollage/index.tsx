import React from 'react';
import styles from './styles.module.css';

const ImageList = [
  { src: 'img/docs/harmonizer_biesty.png', alt: 'Harmonizer Biesty' },
  { src: 'img/docs/sentinel_kandinsky.png', alt: 'Sentinel Kandinsky' },
  { src: 'img/docs/architect.png', alt: 'Architect' },
  { src: 'img/docs/librarian_biesty.png', alt: 'Librarian Biesty' },
  { src: 'img/docs/harmonizer_kandinsky.png', alt: 'Harmonizer Kandinsky' },
  { src: 'img/docs/sentinel.png', alt: 'Sentinel' },
  { src: 'img/docs/harmonizer_biesty_clean.png', alt: 'Harmonizer Biesty Clean' },
  { src: 'img/docs/librarian_kandinsky.png', alt: 'Librarian Kandinsky' },
  { src: 'img/docs/architect_kandinsky.png', alt: 'Architect Kandinsky' },
];

export default function HomepageCollage(): JSX.Element {
  // Duplicate the list to create a seamless loop
  const sliderImages = [...ImageList, ...ImageList];

  return (
    <div className={styles.sliderContainer}>
      <div className={styles.sliderTrack}>
        {sliderImages.map((props, idx) => (
          <div key={idx} className={styles.slide}>
            <img 
              src={props.src} 
              alt={props.alt} 
              className={styles.image}
            />
          </div>
        ))}
      </div>
    </div>
  );
}
