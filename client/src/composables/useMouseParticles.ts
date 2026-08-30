/**
 * 鼠标粒子跟随 composable
 * 鼠标移动时在容器内产生红色粒子拖尾效果
 */
import { onMounted, onUnmounted, type Ref } from 'vue'

interface Particle {
  x: number
  y: number
  vx: number
  vy: number
  life: number
  maxLife: number
  size: number
  hue: number
}

export function useMouseParticles(containerRef: Ref<HTMLElement | null>) {
  let canvas: HTMLCanvasElement | null = null
  let ctx: CanvasRenderingContext2D | null = null
  let particles: Particle[] = []
  let animId = 0
  let mouseX = 0
  let mouseY = 0
  let lastEmit = 0

  function createParticle(x: number, y: number) {
    const angle = Math.random() * Math.PI * 2
    const speed = Math.random() * 2 + 0.5
    particles.push({
      x,
      y,
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed,
      life: 1,
      maxLife: 0.6 + Math.random() * 0.4,
      size: 2 + Math.random() * 4,
      hue: Math.random() > 0.5 ? 0 : 25 // red or orange
    })
  }

  function animate() {
    if (!ctx || !canvas) return
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    for (let i = particles.length - 1; i >= 0; i--) {
      const p = particles[i]
      p.x += p.vx
      p.y += p.vy
      p.vy += 0.02 // slight gravity
      p.life -= 0.02

      if (p.life <= 0) {
        particles.splice(i, 1)
        continue
      }

      const alpha = p.life * 0.8
      ctx.beginPath()
      ctx.arc(p.x, p.y, p.size * p.life, 0, Math.PI * 2)
      ctx.fillStyle = `hsla(${p.hue}, 85%, 55%, ${alpha})`
      ctx.fill()

      // glow
      ctx.beginPath()
      ctx.arc(p.x, p.y, p.size * p.life * 2, 0, Math.PI * 2)
      ctx.fillStyle = `hsla(${p.hue}, 85%, 55%, ${alpha * 0.15})`
      ctx.fill()
    }

    // keep max particles reasonable
    if (particles.length > 150) {
      particles = particles.slice(-100)
    }

    animId = requestAnimationFrame(animate)
  }

  function onMouseMove(e: MouseEvent) {
    if (!containerRef.value) return
    const rect = containerRef.value.getBoundingClientRect()
    mouseX = e.clientX - rect.left
    mouseY = e.clientY - rect.top

    const now = Date.now()
    if (now - lastEmit > 16) { // ~60fps emit
      createParticle(mouseX, mouseY)
      if (Math.random() > 0.6) {
        createParticle(mouseX + (Math.random() - 0.5) * 10, mouseY + (Math.random() - 0.5) * 10)
      }
      lastEmit = now
    }
  }

  onMounted(() => {
    if (!containerRef.value) return
    canvas = document.createElement('canvas')
    canvas.style.cssText = 'position:absolute;top:0;left:0;width:100%;height:100%;pointer-events:none;z-index:999;'
    containerRef.value.style.position = 'relative'
    containerRef.value.appendChild(canvas)

    const resize = () => {
      if (!canvas || !containerRef.value) return
      const rect = containerRef.value.getBoundingClientRect()
      canvas.width = rect.width
      canvas.height = rect.height
    }
    resize()

    ctx = canvas.getContext('2d')
    containerRef.value.addEventListener('mousemove', onMouseMove)
    window.addEventListener('resize', resize)
    animate()
  })

  onUnmounted(() => {
    containerRef.value?.removeEventListener('mousemove', onMouseMove)
    cancelAnimationFrame(animId)
    canvas?.remove()
    particles = []
  })
}
