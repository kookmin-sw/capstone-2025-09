import React, { useRef, useEffect, useState } from 'react';
import { useFrame, useLoader } from '@react-three/fiber';
import * as THREE from 'three';

// 텍스처 이미지
import sirens from '../assets/sirens.jpeg';
import cosmic from '../assets/cosmic-fusion.jpeg';
import deepOcean from '../assets/deep-ocean.jpeg';
import hollogram from '../assets/hollogram.jpeg';
import imaginarium from '../assets/imaginarium.jpeg';
import iridescent from '../assets/iridescent.jpeg';

const texturesList = [
  sirens,
  cosmic,
  deepOcean,
  hollogram,
  imaginarium,
  iridescent,
];

const vertexShader = `
  uniform float uTime;
  varying vec2 vUv;

  float noise(vec3 p) {
    return sin(p.x * 1.0 + uTime * 0.6) * 0.25 +
           sin(p.y * 1.0 + uTime * 0.4) * 0.2;
  }

  float getDisplacement(vec3 position) {
    return noise(position * 2.2 + vec3(uTime * 0.4)) * 0.4;
  }

  void main() {
    vUv = uv;
    vec3 displaced = position + normal * getDisplacement(position);
    gl_Position = projectionMatrix * modelViewMatrix * vec4(displaced, 1.0);
  }
`;

const fragmentShader = `
  varying vec2 vUv;
  uniform sampler2D uTexture;

  void main() {
    vec3 texColor = texture2D(uTexture, vUv).rgb;
    gl_FragColor = vec4(texColor, 1.0);
  }
`;

const WaveSphere = () => {
  const meshRef = useRef();
  const shaderRef = useRef();
  const textures = useLoader(THREE.TextureLoader, texturesList);

  const [ready, setReady] = useState(false);
  const textureRef = useRef(0);

  useEffect(() => {
    textures.forEach((tex) => {
      tex.wrapS = tex.wrapT = THREE.RepeatWrapping;
      tex.repeat.set(2, 2);
    });
    setReady(true);
  }, [textures]);

  useEffect(() => {
    if (!ready || textures.length === 0) return;

    const updateTexture = () => {
      textureRef.current = (textureRef.current + 1) % textures.length;
      if (shaderRef.current) {
        shaderRef.current.uniforms.uTexture.value =
          textures[textureRef.current];
      }
    };

    const interval = setInterval(updateTexture, 4000);
    return () => clearInterval(interval);
  }, [ready, textures]);

  useFrame(({ clock }) => {
    if (meshRef.current) {
      meshRef.current.rotation.y += 0.0085;
      meshRef.current.rotation.x += 0.0045;
    }

    if (shaderRef.current) {
      shaderRef.current.uniforms.uTime.value = clock.getElapsedTime();
      shaderRef.current.uniforms.uTexture.value = textures[textureRef.current];
    }
  });

  if (!ready) return null;

  return (
    <mesh ref={meshRef}>
      <sphereGeometry args={[2, 128, 128]} />
      <shaderMaterial
        ref={shaderRef}
        vertexShader={vertexShader}
        fragmentShader={fragmentShader}
        uniforms={{
          uTime: { value: 0 },
          uTexture: { value: textures[0] },
        }}
        side={THREE.FrontSide}
      />
    </mesh>
  );
};

export default WaveSphere;
