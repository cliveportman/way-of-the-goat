// ----------------------------------------------------------------
// Load WASM
// ----------------------------------------------------------------
import init, { analyze_gpx } from './pkg/vo2.js';

let wasmReady = false;
let pendingFile = null;

(async () => {
  try {
    await init();
    wasmReady = true;
    updateBtn();
  } catch (e) {
    console.error('WASM load failed:', e);
    showStatus('error', 'Failed to load WASM module. See console for details.');
  }
})();

// ----------------------------------------------------------------
// File handling
// ----------------------------------------------------------------
const dropZone = document.getElementById('dropZone');
const fileInput = document.getElementById('fileInput');
const analyzeBtn = document.getElementById('analyzeBtn');

dropZone.addEventListener('dragover', e => { e.preventDefault(); dropZone.classList.add('drag-over'); });
dropZone.addEventListener('dragleave', () => dropZone.classList.remove('drag-over'));
dropZone.addEventListener('drop', e => {
  e.preventDefault();
  dropZone.classList.remove('drag-over');
  if (e.dataTransfer.files[0]) handleFile(e.dataTransfer.files[0]);
});
fileInput.addEventListener('change', () => {
  if (fileInput.files[0]) handleFile(fileInput.files[0]);
});

function handleFile(f) {
  pendingFile = f;
  document.getElementById('fileName').textContent = f.name;
  document.getElementById('fileSize').textContent = formatBytes(f.size);
  document.getElementById('fileInfo').classList.add('visible');
  hideStatus();
  document.getElementById('results').classList.remove('visible');
  updateBtn();
}

function updateBtn() {
  analyzeBtn.disabled = !(wasmReady && pendingFile);
}

function formatBytes(n) {
  if (n < 1024) return n + ' B';
  if (n < 1048576) return (n / 1024).toFixed(1) + ' KB';
  return (n / 1048576).toFixed(1) + ' MB';
}

// ----------------------------------------------------------------
// Analyze
// ----------------------------------------------------------------
analyzeBtn.addEventListener('click', async () => {
  if (!pendingFile || !wasmReady) return;

  analyzeBtn.disabled = true;
  showStatus('loading', 'Parsing GPX and calculating VO2 max…');
  document.getElementById('results').classList.remove('visible');

  try {
    const text = await pendingFile.text();
    const weightKg = parseFloat(document.getElementById('weightInput').value) || 0;
    const maxHr = parseInt(document.getElementById('maxHrInput').value, 10) || 0;

    // Run in a microtask so the loading UI renders
    await new Promise(r => setTimeout(r, 10));

    const json = analyze_gpx(text, weightKg, maxHr);
    const data = JSON.parse(json);

    if (data.error) {
      showStatus('error', data.error);
    } else {
      hideStatus();
      renderResults(data, weightKg);
    }
  } catch (e) {
    console.error(e);
    showStatus('error', 'Something went wrong analysing the file. Check the console for details.');
  } finally {
    analyzeBtn.disabled = false;
  }
});

// ----------------------------------------------------------------
// Render
// ----------------------------------------------------------------
function renderResults(d, weightKg) {
  const results = document.getElementById('results');
  results.classList.add('visible');

  // ---- Stats ----
  const grid = document.getElementById('statGrid');
  grid.innerHTML = '';

  const paceStr = fmtPace(d.avg_pace_min_per_km);
  const durationStr = fmtDuration(d.total_duration_min);

  const stats = [
    { label: 'Distance', value: d.total_distance_km.toFixed(2), unit: 'km' },
    { label: 'Duration', value: durationStr, unit: '' },
    { label: 'Avg Pace', value: paceStr, unit: '/km' },
    { label: 'Elev Gain', value: d.elevation_gain_m, unit: 'm', hidden: !d.has_elevation_data },
    { label: 'Points', value: d.point_count.toLocaleString(), unit: '' },
    { label: 'Avg HR', value: d.avg_hr ? Math.round(d.avg_hr) : '–', unit: d.avg_hr ? 'bpm' : '', hidden: !d.has_hr_data },
    { label: 'Max HR', value: d.max_hr_recorded ?? '–', unit: d.max_hr_recorded ? 'bpm' : '', hidden: !d.has_hr_data },
  ];

  for (const s of stats) {
    if (s.hidden) continue;
    const card = document.createElement('div');
    card.className = 'stat-card';
    const label = document.createElement('div');
    label.className = 'stat-label';
    label.textContent = s.label;
    const value = document.createElement('div');
    value.className = 'stat-value';
    value.textContent = s.value;
    const unit = document.createElement('span');
    unit.className = 'stat-unit';
    unit.textContent = s.unit;
    value.appendChild(unit);
    card.appendChild(label);
    card.appendChild(value);
    grid.appendChild(card);
  }

  // Data availability notices
  const noticesEl = document.createElement('div');
  noticesEl.style.marginTop = '10px';
  if (!d.has_hr_data) {
    const notice = document.createElement('div');
    notice.className = 'notice';
    notice.textContent = '⚠️ No heart rate data found in this GPX file. Add a HR monitor for the most accurate VO2 max estimate.';
    noticesEl.appendChild(notice);
  }
  if (!d.has_time_data) {
    const notice = document.createElement('div');
    notice.className = 'notice';
    notice.textContent = '⚠️ No timestamps in GPX file — pace and time-based analysis unavailable.';
    noticesEl.appendChild(notice);
  }
  grid.after(noticesEl);

  // ---- Estimates ----
  const vo2Title = document.getElementById('vo2Title');
  const estimatesEl = document.getElementById('estimatesContainer');
  estimatesEl.replaceChildren();

  if (d.estimates.length > 0) {
    vo2Title.hidden = false;
    for (const e of d.estimates) {
      const card = document.createElement('div');
      card.className = 'estimate-card';
      const pct = e.confidence_pct;
      const barColor = pct >= 70 ? '#4ade80' : pct >= 45 ? '#fbbf24' : '#666';
      const textColor = pct >= 70 ? '#4ade80' : pct >= 45 ? '#fbbf24' : '#888';

      const left = document.createElement('div');
      const methodEl = document.createElement('div');
      methodEl.className = 'estimate-method';
      methodEl.textContent = e.method;
      const notesEl = document.createElement('div');
      notesEl.className = 'estimate-notes';
      notesEl.textContent = e.notes;
      left.appendChild(methodEl);
      left.appendChild(notesEl);

      const right = document.createElement('div');
      right.className = 'estimate-right';
      const valueEl = document.createElement('div');
      valueEl.className = 'estimate-value';
      valueEl.textContent = e.value;
      const unitEl = document.createElement('div');
      unitEl.className = 'estimate-unit';
      unitEl.textContent = 'mL/kg/min';

      const badgeWrap = document.createElement('div');
      const badge = document.createElement('span');
      badge.className = 'badge';
      badge.style.color = textColor;
      badge.style.background = barColor + '18';
      badge.style.border = '1px solid ' + barColor + '44';
      const barTrack = document.createElement('span');
      barTrack.className = 'badge-bar-track';
      const barFill = document.createElement('span');
      barFill.className = 'badge-bar-fill';
      barFill.style.width = pct + '%';
      barFill.style.background = barColor;
      barTrack.appendChild(barFill);
      badge.appendChild(barTrack);
      badge.appendChild(document.createTextNode(` ${pct}% confidence`));
      badgeWrap.appendChild(badge);

      right.appendChild(valueEl);
      right.appendChild(unitEl);
      right.appendChild(badgeWrap);

      if (weightKg > 0) {
        const absEl = document.createElement('div');
        absEl.className = 'estimate-unit';
        absEl.style.marginTop = '6px';
        absEl.textContent = (e.value * weightKg / 1000).toFixed(2) + ' L/min absolute';
        right.appendChild(absEl);
      }

      card.appendChild(left);
      card.appendChild(right);
      estimatesEl.appendChild(card);
    }
  } else {
    vo2Title.hidden = false;
    const noData = document.createElement('div');
    noData.className = 'notice';
    noData.textContent = 'Could not calculate VO2 max — need either HR data or time data in the GPX file.';
    estimatesEl.appendChild(noData);
  }

  // ---- Fitness category ----
  const fitSection = document.getElementById('fitnessSection');
  fitSection.replaceChildren();
  if (d.fitness_category) {
    const bestEstimate = d.estimates.length
      ? d.estimates.reduce((a, b) => b.confidence_pct > a.confidence_pct ? b : a)
      : null;
    const vo2val = bestEstimate ? bestEstimate.value : 0;
    const pct = Math.min(100, Math.max(0, (vo2val - 20) / (85 - 20) * 100));

    const title = document.createElement('div');
    title.className = 'section-title';
    title.textContent = 'Fitness Category';

    const banner = document.createElement('div');
    banner.className = 'fitness-banner';

    const info = document.createElement('div');
    const catName = document.createElement('div');
    catName.className = 'fitness-cat-name';
    catName.textContent = d.fitness_category;
    const catDesc = document.createElement('div');
    catDesc.className = 'fitness-cat-desc';
    catDesc.textContent = d.fitness_description;
    info.appendChild(catName);
    info.appendChild(catDesc);

    if (bestEstimate) {
      const basis = document.createElement('div');
      basis.className = 'fitness-cat-desc';
      basis.style.marginTop = '4px';
      basis.style.fontSize = '12px';
      basis.appendChild(document.createTextNode('Based on '));
      const strong = document.createElement('strong');
      strong.style.color = 'var(--text)';
      strong.textContent = bestEstimate.method;
      basis.appendChild(strong);
      basis.appendChild(document.createTextNode(` \u2014 ${bestEstimate.confidence_pct}% confidence`));
      info.appendChild(basis);
    }

    const scale = document.createElement('div');
    scale.className = 'vo2-scale';
    scale.style.minWidth = '120px';
    scale.style.maxWidth = '240px';
    const marker = document.createElement('div');
    marker.className = 'vo2-scale-marker';
    marker.style.left = pct + '%';
    scale.appendChild(marker);

    banner.appendChild(info);
    banner.appendChild(scale);

    const legend = document.createElement('div');
    legend.style.display = 'flex';
    legend.style.justifyContent = 'space-between';
    legend.style.fontSize = '11px';
    legend.style.color = 'var(--muted)';
    legend.style.marginTop = '4px';
    legend.style.padding = '0 2px';
    for (const label of ['20 (Poor)', '40 (Fair)', '60 (Superior)', '85+ (Elite)']) {
      const span = document.createElement('span');
      span.textContent = label;
      legend.appendChild(span);
    }

    fitSection.appendChild(title);
    fitSection.appendChild(banner);
    fitSection.appendChild(legend);
  }

  // ---- Peak 1 km ----
  const peak1kmTitle = document.getElementById('peak1kmTitle');
  const peak1kmEl = document.getElementById('peak1kmSection');
  peak1kmEl.replaceChildren();
  if (d.peak_1km) {
    peak1kmTitle.hidden = false;
    const p = d.peak_1km;
    const paceMin = Math.floor(p.pace_min_per_km);
    const paceSec = Math.round((p.pace_min_per_km - paceMin) * 60).toString().padStart(2, '0');
    const gradeStr = p.avg_grade_pct >= 0
      ? `+${p.avg_grade_pct.toFixed(1)}%`
      : `${p.avg_grade_pct.toFixed(1)}%`;
    const gradeColor = p.avg_grade_pct > 2 ? 'var(--warn)' : p.avg_grade_pct < -2 ? '#60a5fa' : 'var(--muted)';

    const card = document.createElement('div');
    card.className = 'estimate-card';

    const left = document.createElement('div');
    left.style.flex = '1';
    const methodEl = document.createElement('div');
    methodEl.className = 'estimate-method';
    methodEl.textContent = 'Highest-intensity kilometre';
    left.appendChild(methodEl);

    const statsRow = document.createElement('div');
    statsRow.className = 'estimate-notes';
    statsRow.style.marginTop = '6px';
    statsRow.style.display = 'flex';
    statsRow.style.gap = '18px';
    statsRow.style.flexWrap = 'wrap';

    function addStat(emoji, label, value, color) {
      const span = document.createElement('span');
      span.appendChild(document.createTextNode(emoji + ' ' + label + ' '));
      const strong = document.createElement('strong');
      strong.style.color = color;
      strong.textContent = value;
      span.appendChild(strong);
      statsRow.appendChild(span);
    }
    addStat('📍', 'Starting at', p.start_distance_km + ' km', 'var(--text)');
    addStat('⚡', 'Pace', paceMin + ':' + paceSec + ' /km', 'var(--text)');
    addStat('⛰', 'Gradient', gradeStr, gradeColor);
    if (p.avg_hr) addStat('♥', 'Avg HR', p.avg_hr + ' bpm', 'var(--text)');
    left.appendChild(statsRow);

    const desc = document.createElement('div');
    desc.className = 'estimate-notes';
    desc.style.marginTop = '8px';
    let descText = 'VO2 expressed is the oxygen cost of running that kilometre — not your VO2 max, but the highest metabolic demand you placed on yourself during this activity.';
    if (p.vo2max_est) descText += ' The implied VO2 max extrapolates your HR during that km to your entered HRmax.';
    desc.textContent = descText;
    left.appendChild(desc);

    const right = document.createElement('div');
    right.className = 'estimate-right';
    const valueEl = document.createElement('div');
    valueEl.className = 'estimate-value';
    valueEl.textContent = p.vo2_expressed;
    const unitEl = document.createElement('div');
    unitEl.className = 'estimate-unit';
    unitEl.textContent = 'mL/kg/min expressed';
    right.appendChild(valueEl);
    right.appendChild(unitEl);

    if (p.vo2max_est) {
      const impliedWrap = document.createElement('div');
      impliedWrap.style.marginTop = '10px';
      const impliedVal = document.createElement('div');
      impliedVal.style.fontSize = '22px';
      impliedVal.style.fontWeight = '700';
      impliedVal.style.color = '#a78bfa';
      impliedVal.textContent = p.vo2max_est;
      const impliedUnit = document.createElement('div');
      impliedUnit.className = 'estimate-unit';
      impliedUnit.textContent = 'mL/kg/min implied max';
      impliedWrap.appendChild(impliedVal);
      impliedWrap.appendChild(impliedUnit);
      right.appendChild(impliedWrap);
    }

    card.appendChild(left);
    card.appendChild(right);
    peak1kmEl.appendChild(card);
  }

  // ---- Charts ----
  const chartsTitle = document.getElementById('chartsTitle');
  const chartsEl = document.getElementById('chartsContainer');
  chartsEl.innerHTML = '';

  if (d.chart_points && d.chart_points.length > 1) {
    chartsTitle.hidden = false;

    // Pace + HR chart
    const paceData = d.chart_points.map(p => p.pace_min_per_km).filter(v => v != null && v > 0);
    const hrData = d.chart_points.filter(p => p.hr != null);

    if (paceData.length > 0 || hrData.length > 0) {
      const c = createChartContainer('Pace & Heart Rate');
      chartsEl.appendChild(c.wrapper);
      const legend = document.createElement('div');
      legend.className = 'legend';
      if (paceData.length > 0) {
        const item = document.createElement('div');
        item.className = 'legend-item';
        const dot = document.createElement('div');
        dot.className = 'legend-dot legend-dot--pace';
        item.appendChild(dot);
        item.appendChild(document.createTextNode('Pace (min/km)'));
        legend.appendChild(item);
      }
      if (hrData.length > 0) {
        const item = document.createElement('div');
        item.className = 'legend-item';
        const dot = document.createElement('div');
        dot.className = 'legend-dot legend-dot--hr';
        item.appendChild(dot);
        item.appendChild(document.createTextNode('Heart Rate (bpm)'));
        legend.appendChild(item);
      }
      c.wrapper.appendChild(legend);
      drawPaceHrChart(c.canvas, d.chart_points);
    }

    // Elevation chart
    const eleData = d.chart_points.filter(p => p.elevation_m != null);
    if (eleData.length > 10) {
      const c = createChartContainer('Elevation Profile');
      chartsEl.appendChild(c.wrapper);
      drawElevationChart(c.canvas, d.chart_points);
    }

    // Cardiac drift chart
    if (d.cardiac_drift && d.cardiac_drift.length > 10) {
      const label = d.decoupling_pct != null
        ? `Cardiac Drift — ${d.decoupling_pct.toFixed(1)}% aerobic decoupling`
        : 'Cardiac Drift';
      const c = createChartContainer(label);
      chartsEl.appendChild(c.wrapper);
      const note = document.createElement('div');
      note.className = 'chart-note';
      note.textContent = 'Speed/HR efficiency, normalised to opening baseline (1.0). ' +
        'Falling values mean your heart is working progressively harder for the same pace.';
      c.wrapper.appendChild(note);
      drawDriftChart(c.canvas, d.cardiac_drift, d.decoupling_pct);
    }

    // Descent rate scatter chart
    if (d.descent_points && d.descent_points.length > 5) {
      const c = createChartContainer('Descent Rate by Gradient');
      chartsEl.appendChild(c.wrapper);
      const legend = document.createElement('div');
      legend.className = 'legend';
      const earlyItem = document.createElement('div');
      earlyItem.className = 'legend-item';
      const earlyDot = document.createElement('div');
      earlyDot.className = 'legend-dot';
      earlyDot.style.background = 'hsl(120,65%,50%)';
      earlyItem.appendChild(earlyDot);
      earlyItem.appendChild(document.createTextNode('Early'));
      const lateItem = document.createElement('div');
      lateItem.className = 'legend-item';
      const lateDot = document.createElement('div');
      lateDot.className = 'legend-dot';
      lateDot.style.background = 'hsl(0,65%,55%)';
      lateItem.appendChild(lateDot);
      lateItem.appendChild(document.createTextNode('Late'));
      legend.appendChild(earlyItem);
      legend.appendChild(lateItem);
      c.wrapper.appendChild(legend);
      const note = document.createElement('div');
      note.className = 'chart-note';
      note.textContent = 'Each dot is a downhill segment. Colour shifts green → red through the activity. ' +
        'A lower cloud late in the run indicates leg fatigue on descents.';
      c.wrapper.appendChild(note);
      drawDescentChart(c.canvas, d.descent_points);
    }
  }
}

function createChartContainer(title) {
  const wrapper = document.createElement('div');
  wrapper.className = 'chart-container';
  const titleEl = document.createElement('div');
  titleEl.className = 'chart-title';
  titleEl.textContent = title;
  const canvas = document.createElement('canvas');
  canvas.height = 200;
  wrapper.appendChild(titleEl);
  wrapper.appendChild(canvas);
  return { wrapper, canvas };
}

// ----------------------------------------------------------------
// Chart drawing
// ----------------------------------------------------------------
function drawPaceHrChart(canvas, points) {
  const dpr = window.devicePixelRatio || 1;
  const W = canvas.parentElement.clientWidth - 40;
  const H = 200;
  canvas.style.width = W + 'px';
  canvas.style.height = H + 'px';
  canvas.width = W * dpr;
  canvas.height = H * dpr;

  const ctx = canvas.getContext('2d');
  ctx.scale(dpr, dpr);

  const pad = { top: 10, right: 60, bottom: 30, left: 55 };
  const iw = W - pad.left - pad.right;
  const ih = H - pad.top - pad.bottom;

  const pacePoints = points.filter(p => p.pace_min_per_km != null && p.pace_min_per_km > 0 && p.pace_min_per_km < 30);
  const hrPoints = points.filter(p => p.hr != null);
  const maxDist = points[points.length - 1].distance_km || 1;

  // Y scales — pace is inverted (lower = faster)
  const paceMin = pacePoints.length ? Math.min(...pacePoints.map(p => p.pace_min_per_km)) * 0.95 : 3;
  const paceMax = pacePoints.length ? Math.max(...pacePoints.map(p => p.pace_min_per_km)) * 1.02 : 10;
  const hrMin = hrPoints.length ? Math.min(...hrPoints.map(p => p.hr)) * 0.97 : 60;
  const hrMax = hrPoints.length ? Math.max(...hrPoints.map(p => p.hr)) * 1.02 : 200;

  const xScale = d => pad.left + (d / maxDist) * iw;
  const paceScale = v => pad.top + ((v - paceMin) / (paceMax - paceMin)) * ih;
  const hrScale = v => pad.top + (1 - (v - hrMin) / (hrMax - hrMin)) * ih;

  // Grid
  ctx.strokeStyle = '#222';
  ctx.lineWidth = 1;
  for (let i = 0; i <= 4; i++) {
    const y = pad.top + (ih / 4) * i;
    ctx.beginPath(); ctx.moveTo(pad.left, y); ctx.lineTo(pad.left + iw, y); ctx.stroke();
  }

  // HR area fill
  if (hrPoints.length > 1) {
    ctx.beginPath();
    ctx.moveTo(xScale(hrPoints[0].distance_km), hrScale(hrPoints[0].hr));
    for (const p of hrPoints) ctx.lineTo(xScale(p.distance_km), hrScale(p.hr));
    ctx.lineTo(xScale(hrPoints[hrPoints.length - 1].distance_km), pad.top + ih);
    ctx.lineTo(xScale(hrPoints[0].distance_km), pad.top + ih);
    ctx.closePath();
    ctx.fillStyle = 'rgba(248,113,113,0.10)';
    ctx.fill();

    ctx.beginPath();
    ctx.moveTo(xScale(hrPoints[0].distance_km), hrScale(hrPoints[0].hr));
    for (const p of hrPoints) ctx.lineTo(xScale(p.distance_km), hrScale(p.hr));
    ctx.strokeStyle = '#f87171';
    ctx.lineWidth = 1.5;
    ctx.stroke();
  }

  // Pace line (smoothed with small window)
  if (pacePoints.length > 1) {
    const smoothed = smoothArray(pacePoints.map(p => p.pace_min_per_km), 5);
    ctx.beginPath();
    ctx.moveTo(xScale(pacePoints[0].distance_km), paceScale(smoothed[0]));
    for (let i = 1; i < pacePoints.length; i++) {
      ctx.lineTo(xScale(pacePoints[i].distance_km), paceScale(smoothed[i]));
    }
    ctx.strokeStyle = '#4ade80';
    ctx.lineWidth = 2;
    ctx.stroke();
  }

  // Axes labels
  ctx.fillStyle = '#666';
  ctx.font = '11px -apple-system, sans-serif';

  // X axis (distance)
  const distStep = maxDist <= 5 ? 1 : maxDist <= 15 ? 2 : maxDist <= 25 ? 5 : 10;
  for (let d = 0; d <= maxDist; d += distStep) {
    const x = xScale(d);
    ctx.textAlign = 'center';
    ctx.fillText(d + 'km', x, H - 6);
  }

  // Left Y axis (pace)
  if (pacePoints.length > 0) {
    ctx.fillStyle = '#4ade80';
    const paceSteps = 4;
    for (let i = 0; i <= paceSteps; i++) {
      const v = paceMin + (paceMax - paceMin) * (i / paceSteps);
      const y = paceScale(v);
      ctx.textAlign = 'right';
      ctx.fillText(fmtPace(v), pad.left - 6, y + 4);
    }
  }

  // Right Y axis (HR)
  if (hrPoints.length > 0) {
    ctx.fillStyle = '#f87171';
    const hrSteps = 4;
    for (let i = 0; i <= hrSteps; i++) {
      const v = hrMin + (hrMax - hrMin) * (i / hrSteps);
      const y = hrScale(v);
      ctx.textAlign = 'left';
      ctx.fillText(Math.round(v), pad.left + iw + 6, y + 4);
    }
  }
}

function drawElevationChart(canvas, points) {
  const dpr = window.devicePixelRatio || 1;
  const W = canvas.parentElement.clientWidth - 40;
  const H = 130;
  canvas.style.width = W + 'px';
  canvas.style.height = H + 'px';
  canvas.width = W * dpr;
  canvas.height = H * dpr;

  const ctx = canvas.getContext('2d');
  ctx.scale(dpr, dpr);

  const pad = { top: 10, right: 20, bottom: 24, left: 55 };
  const iw = W - pad.left - pad.right;
  const ih = H - pad.top - pad.bottom;

  const elePts = points.filter(p => p.elevation_m != null);
  if (elePts.length < 2) return;

  const maxDist = points[points.length - 1].distance_km || 1;
  const eleMin = Math.min(...elePts.map(p => p.elevation_m)) - 5;
  const eleMax = Math.max(...elePts.map(p => p.elevation_m)) + 5;

  const xScale = d => pad.left + (d / maxDist) * iw;
  const yScale = v => pad.top + (1 - (v - eleMin) / (eleMax - eleMin)) * ih;

  // Filled area
  ctx.beginPath();
  ctx.moveTo(xScale(elePts[0].distance_km), yScale(elePts[0].elevation_m));
  for (const p of elePts) ctx.lineTo(xScale(p.distance_km), yScale(p.elevation_m));
  ctx.lineTo(xScale(elePts[elePts.length - 1].distance_km), pad.top + ih);
  ctx.lineTo(xScale(elePts[0].distance_km), pad.top + ih);
  ctx.closePath();

  const grad = ctx.createLinearGradient(0, pad.top, 0, pad.top + ih);
  grad.addColorStop(0, 'rgba(96,165,250,0.35)');
  grad.addColorStop(1, 'rgba(96,165,250,0.04)');
  ctx.fillStyle = grad;
  ctx.fill();

  ctx.beginPath();
  ctx.moveTo(xScale(elePts[0].distance_km), yScale(elePts[0].elevation_m));
  for (const p of elePts) ctx.lineTo(xScale(p.distance_km), yScale(p.elevation_m));
  ctx.strokeStyle = '#60a5fa';
  ctx.lineWidth = 2;
  ctx.stroke();

  // Labels
  ctx.fillStyle = '#666';
  ctx.font = '11px -apple-system, sans-serif';
  const eleSteps = 3;
  for (let i = 0; i <= eleSteps; i++) {
    const v = eleMin + (eleMax - eleMin) * (i / eleSteps);
    ctx.textAlign = 'right';
    ctx.fillText(Math.round(v) + 'm', pad.left - 6, yScale(v) + 4);
  }

  const distStep = maxDist <= 5 ? 1 : maxDist <= 15 ? 2 : maxDist <= 25 ? 5 : 10;
  for (let d = 0; d <= maxDist; d += distStep) {
    ctx.textAlign = 'center';
    ctx.fillText(d + 'km', xScale(d), H - 5);
  }
}

function drawDriftChart(canvas, driftPoints, decouplingPct) {
  const dpr = window.devicePixelRatio || 1;
  const W = canvas.parentElement.clientWidth - 40;
  const H = 200;
  canvas.style.width = W + 'px';
  canvas.style.height = H + 'px';
  canvas.width = W * dpr;
  canvas.height = H * dpr;

  const ctx = canvas.getContext('2d');
  ctx.scale(dpr, dpr);

  const pad = { top: 14, right: 20, bottom: 30, left: 52 };
  const iw = W - pad.left - pad.right;
  const ih = H - pad.top - pad.bottom;

  const maxDist = driftPoints[driftPoints.length - 1].distance_km || 1;
  const effVals = driftPoints.map(p => p.efficiency);
  const rawMin = Math.min(...effVals);
  const rawMax = Math.max(...effVals);
  const yMin = Math.min(rawMin * 0.97, 0.85);
  const yMax = Math.max(rawMax * 1.03, 1.12);

  const xScale = d => pad.left + (d / maxDist) * iw;
  const yScale = v => pad.top + (1 - (v - yMin) / (yMax - yMin)) * ih;

  // Grid lines
  ctx.strokeStyle = '#222';
  ctx.lineWidth = 1;
  for (let i = 0; i <= 4; i++) {
    const y = pad.top + (ih / 4) * i;
    ctx.beginPath(); ctx.moveTo(pad.left, y); ctx.lineTo(pad.left + iw, y); ctx.stroke();
  }

  // Baseline at efficiency = 1.0 (dashed)
  const baseY = yScale(1.0);
  ctx.save();
  ctx.setLineDash([4, 4]);
  ctx.strokeStyle = '#445';
  ctx.lineWidth = 1;
  ctx.beginPath(); ctx.moveTo(pad.left, baseY); ctx.lineTo(pad.left + iw, baseY); ctx.stroke();
  ctx.restore();

  // Midpoint vertical divider (dashed)
  const midX = xScale(maxDist / 2);
  ctx.save();
  ctx.setLineDash([4, 4]);
  ctx.strokeStyle = '#445';
  ctx.lineWidth = 1;
  ctx.beginPath(); ctx.moveTo(midX, pad.top); ctx.lineTo(midX, pad.top + ih); ctx.stroke();
  ctx.restore();
  ctx.fillStyle = '#556';
  ctx.font = '10px -apple-system, sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText('½', midX, pad.top + 10);

  // Subtle fill beneath the baseline in the drift zone
  ctx.beginPath();
  ctx.moveTo(xScale(driftPoints[0].distance_km), yScale(driftPoints[0].efficiency));
  for (const p of driftPoints) ctx.lineTo(xScale(p.distance_km), yScale(p.efficiency));
  ctx.lineTo(xScale(driftPoints[driftPoints.length - 1].distance_km), baseY);
  ctx.lineTo(xScale(driftPoints[0].distance_km), baseY);
  ctx.closePath();
  ctx.fillStyle = 'rgba(167,139,250,0.07)';
  ctx.fill();

  // Efficiency line
  ctx.beginPath();
  ctx.moveTo(xScale(driftPoints[0].distance_km), yScale(driftPoints[0].efficiency));
  for (const p of driftPoints) ctx.lineTo(xScale(p.distance_km), yScale(p.efficiency));
  ctx.strokeStyle = '#a78bfa';
  ctx.lineWidth = 2;
  ctx.stroke();

  // Axes
  ctx.fillStyle = '#666';
  ctx.font = '11px -apple-system, sans-serif';

  // X axis (distance)
  const distStep = maxDist <= 5 ? 1 : maxDist <= 15 ? 2 : maxDist <= 25 ? 5 : 10;
  for (let d = 0; d <= maxDist; d += distStep) {
    ctx.textAlign = 'center';
    ctx.fillText(d + 'km', xScale(d), H - 6);
  }

  // Y axis (efficiency)
  ctx.fillStyle = '#a78bfa';
  for (let i = 0; i <= 4; i++) {
    const v = yMin + (yMax - yMin) * (i / 4);
    ctx.textAlign = 'right';
    ctx.fillText(v.toFixed(2), pad.left - 6, yScale(v) + 4);
  }
}

function drawDescentChart(canvas, points) {
  const dpr = window.devicePixelRatio || 1;
  const W = canvas.parentElement.clientWidth - 40;
  const H = 200;
  canvas.style.width = W + 'px';
  canvas.style.height = H + 'px';
  canvas.width = W * dpr;
  canvas.height = H * dpr;

  const ctx = canvas.getContext('2d');
  ctx.scale(dpr, dpr);

  const pad = { top: 10, right: 20, bottom: 38, left: 50 };
  const iw = W - pad.left - pad.right;
  const ih = H - pad.top - pad.bottom;

  // grade_pct is negative; display as absolute gradient on x-axis
  const grades = points.map(p => Math.abs(p.grade_pct));
  const speeds = points.map(p => p.speed_kmh);

  const gradeMin = 0;
  const gradeMax = Math.min(50, Math.max(...grades) * 1.05);
  const speedMin = Math.max(0, Math.min(...speeds) * 0.9);
  const speedMax = Math.max(...speeds) * 1.05;

  const xScale = g => pad.left + ((g - gradeMin) / (gradeMax - gradeMin || 1)) * iw;
  const yScale = s => pad.top + (1 - (s - speedMin) / (speedMax - speedMin || 1)) * ih;

  // Grid lines
  ctx.strokeStyle = '#222';
  ctx.lineWidth = 1;
  for (let i = 0; i <= 4; i++) {
    const y = pad.top + (ih / 4) * i;
    ctx.beginPath(); ctx.moveTo(pad.left, y); ctx.lineTo(pad.left + iw, y); ctx.stroke();
  }

  // Scatter points — colour by progress (green=early, red=late)
  for (const pt of points) {
    const x = xScale(Math.abs(pt.grade_pct));
    const y = yScale(pt.speed_kmh);
    const hue = Math.round((1 - pt.progress) * 120); // 120=green, 0=red
    ctx.beginPath();
    ctx.arc(x, y, 2.5, 0, Math.PI * 2);
    ctx.fillStyle = `hsla(${hue},65%,52%,0.65)`;
    ctx.fill();
  }

  // Axes
  ctx.fillStyle = '#666';
  ctx.font = '11px -apple-system, sans-serif';

  // X axis (gradient %)
  const gradeStep = gradeMax <= 15 ? 5 : gradeMax <= 30 ? 10 : 15;
  const startGrade = Math.ceil(gradeMin / gradeStep) * gradeStep;
  for (let g = startGrade; g <= gradeMax; g += gradeStep) {
    ctx.textAlign = 'center';
    ctx.fillText(g + '%', xScale(g), H - 20);
  }
  ctx.textAlign = 'center';
  ctx.fillText('Gradient', pad.left + iw / 2, H - 4);

  // Y axis (speed km/h)
  const speedRange = speedMax - speedMin;
  const speedStep = speedRange <= 6 ? 2 : speedRange <= 15 ? 3 : 5;
  const startSpeed = Math.ceil(speedMin / speedStep) * speedStep;
  for (let s = startSpeed; s <= speedMax; s += speedStep) {
    ctx.textAlign = 'right';
    ctx.fillText(s.toFixed(0), pad.left - 6, yScale(s) + 4);
  }

  // Y axis label
  ctx.save();
  ctx.translate(12, pad.top + ih / 2);
  ctx.rotate(-Math.PI / 2);
  ctx.textAlign = 'center';
  ctx.fillText('km/h', 0, 0);
  ctx.restore();
}

// ----------------------------------------------------------------
// Helpers
// ----------------------------------------------------------------
function fmtPace(minPerKm) {
  if (!minPerKm || minPerKm <= 0 || minPerKm > 30) return '–';
  const m = Math.floor(minPerKm);
  const s = Math.round((minPerKm - m) * 60);
  return `${m}:${String(s).padStart(2, '0')}`;
}

function fmtDuration(totalMin) {
  const h = Math.floor(totalMin / 60);
  const m = Math.floor(totalMin % 60);
  const s = Math.round((totalMin * 60) % 60);
  if (h > 0) return `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  return `${m}:${String(s).padStart(2, '0')}`;
}

function smoothArray(arr, window) {
  const half = Math.floor(window / 2);
  return arr.map((_, i) => {
    const start = Math.max(0, i - half);
    const end = Math.min(arr.length - 1, i + half);
    let sum = 0;
    for (let j = start; j <= end; j++) sum += arr[j];
    return sum / (end - start + 1);
  });
}

function showStatus(type, msg) {
  const el = document.getElementById('status');
  el.className = 'status visible ' + type;
  el.innerHTML = '';
  if (type === 'loading') {
    const spinner = document.createElement('div');
    spinner.className = 'spinner';
    el.appendChild(spinner);
  }
  const span = document.createElement('span');
  span.textContent = type === 'loading' ? msg : '\u26A0\uFE0F ' + msg;
  el.appendChild(span);
}

function hideStatus() {
  document.getElementById('status').classList.remove('visible');
}
