import * as THREE from 'three';
import { useRef, useEffect } from 'react';

const ThreeWaveVisualizer = ({ analyser }) => {
  const mountRef = useRef();
  const meshRef = useRef();
  const animationIdRef = useRef();

  useEffect(() => {
    if (!analyser) return;

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(75, 1, 0.1, 1000);
    camera.position.z = 50;

    const renderer = new THREE.WebGLRenderer({ alpha: true });
    renderer.setSize(300, 300);
    mountRef.current.appendChild(renderer.domElement);

    const geometry = new THREE.PlaneGeometry(40, 40, 64, 64);
    const material = new THREE.MeshBasicMaterial({ color: 0x6b21a8, wireframe: true });
    const mesh = new THREE.Mesh(geometry, material);
    meshRef.current = mesh;
    scene.add(mesh);

    const bufferLength = analyser.frequencyBinCount;
    const dataArray = new Uint8Array(bufferLength);

    const animate = () => {
      analyser.getByteFrequencyData(dataArray);

      const vertices = geometry.attributes.position;
      for (let i = 0; i < vertices.count; i++) {
        const y = dataArray[i % bufferLength] / 50;
        vertices.setZ(i, y);
      }
      vertices.needsUpdate = true;

      renderer.render(scene, camera);
      animationIdRef.current = requestAnimationFrame(animate);
    };

    animate();

    return () => {
      cancelAnimationFrame(animationIdRef.current);
      renderer.dispose();
      mountRef.current.removeChild(renderer.domElement);
    };
  }, [analyser]);

  return <div ref={mountRef} className="mt-4" />;
};

export default ThreeWaveVisualizer;
