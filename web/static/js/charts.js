'use strict';

/** Диаграммы аналитики: данные приходят с сервера в JSON и рисуются в SVG без сторонних библиотек. */
const SVG_NS = 'http://www.w3.org/2000/svg';
const BAR_MAX = 24;
const number = new Intl.NumberFormat('ru-RU', { maximumFractionDigits: 2 });
const money = new Intl.NumberFormat('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

function compact(value) {
    if (value >= 1_000_000) {
        return `${number.format(Math.round(value / 100_000) / 10)} млн`;
    }
    if (value >= 1_000) {
        return `${number.format(Math.round(value / 100) / 10)} тыс`;
    }
    return number.format(value);
}

function svgElement(name, attributes = {}, text = null) {
    const element = document.createElementNS(SVG_NS, name);
    Object.entries(attributes).forEach(([key, value]) => element.setAttribute(key, value));
    if (text !== null) {
        element.textContent = text;
    }
    return element;
}

/** Верхняя граница оси, округлённая до ближайшего «круглого» значения своего порядка. */
function niceMax(value) {
    if (value <= 0) {
        return 1;
    }
    const power = 10 ** Math.floor(Math.log10(value));
    const step = [1, 2, 2.5, 4, 6, 8, 10].find((candidate) => candidate * power >= value);
    return step * power;
}

/** Прямоугольник со скруглённым концом данных и прямым краем у базовой линии. */
function columnPath(x, y, width, height, radius = 4) {
    const r = Math.min(radius, height, width / 2);
    return `M${x},${y + height}V${y + r}Q${x},${y} ${x + r},${y}H${x + width - r}Q${x + width},${y} ${x + width},${y + r}V${y + height}Z`;
}

function barPath(x, y, width, height, radius = 4) {
    const r = Math.min(radius, width, height / 2);
    return `M${x},${y}H${x + width - r}Q${x + width},${y} ${x + width},${y + r}V${y + height - r}Q${x + width},${y + height} ${x + width - r},${y + height}H${x}Z`;
}

function emptyState(container) {
    const note = document.createElement('p');
    note.className = 'empty';
    note.textContent = 'Нет данных для диаграммы.';
    container.replaceChildren(note);
}

function attachTooltip(container, target, lines) {
    const place = (event) => {
        let tip = container.querySelector('.tooltip');
        if (!tip) {
            tip = document.createElement('div');
            tip.className = 'tooltip';
            container.append(tip);
        }
        const [title, ...rest] = lines;
        const heading = document.createElement('b');
        heading.textContent = title;
        tip.replaceChildren(heading, ...rest.map((line) => {
            const row = document.createElement('div');
            row.textContent = line;
            return row;
        }));
        const box = container.getBoundingClientRect();
        const left = event.clientX - box.left + 12;
        tip.style.left = `${Math.min(left, Math.max(0, box.width - tip.offsetWidth))}px`;
        tip.style.top = `${event.clientY - box.top + 12}px`;
    };
    target.addEventListener('mousemove', place);
    target.addEventListener('mouseleave', () => container.querySelector('.tooltip')?.remove());
}

/** Горизонтальные столбцы одного ряда с подписью значения у конца столбца. */
function drawBars(container, points, format, unit) {
    if (!points?.length) {
        emptyState(container);
        return;
    }
    const width = Math.max(container.clientWidth, 280);
    const labelWidth = Math.min(170, width * 0.4);
    const valueWidth = 80;
    const rowHeight = 38;
    const barHeight = Math.min(BAR_MAX, 20);
    const height = points.length * rowHeight;
    const plot = width - labelWidth - valueWidth;
    const max = Math.max(...points.map((point) => point.value), 1);
    const svg = svgElement('svg', { viewBox: `0 0 ${width} ${height}`, role: 'img' });

    points.forEach((point, index) => {
        const y = index * rowHeight;
        const barTop = y + (rowHeight - barHeight) / 2;
        const barWidth = Math.max(point.value / max * plot, point.value > 0 ? 2 : 0);
        const middle = y + rowHeight / 2 + 4;
        svg.append(svgElement('text', { x: 0, y: middle }, point.label));
        if (barWidth > 0) {
            svg.append(svgElement('path', { class: 'bar-1', d: barPath(labelWidth, barTop, barWidth, barHeight) }));
        }
        svg.append(svgElement('text', { class: 'value-label', x: labelWidth + barWidth + 6, y: middle }, format(point.value)));
        const hit = svgElement('rect', { class: 'hit', x: 0, y, width, height: rowHeight });
        svg.append(hit);
        attachTooltip(container, hit, [point.label, `${money.format(point.value)} ${unit}`.trim()]);
    });
    container.replaceChildren(svg);
}

/** Сгруппированные столбцы двух рядов по месяцам. */
function drawColumns(container, points) {
    if (!points?.length) {
        emptyState(container);
        return;
    }
    const width = Math.max(container.clientWidth, 280);
    const height = 260;
    const margin = { top: 12, right: 8, bottom: 26, left: 62 };
    const plotWidth = width - margin.left - margin.right;
    const plotHeight = height - margin.top - margin.bottom;
    const maxValue = niceMax(Math.max(...points.flatMap((point) => [point.incoming, point.outgoing])));
    const yOf = (value) => margin.top + plotHeight - value / maxValue * plotHeight;
    const slot = plotWidth / points.length;
    const columnWidth = Math.min(BAR_MAX, Math.max(6, (slot - 14) / 2));
    const svg = svgElement('svg', { viewBox: `0 0 ${width} ${height}`, role: 'img' });

    for (let tick = 0; tick <= 4; tick++) {
        const value = maxValue * tick / 4;
        const y = yOf(value);
        svg.append(svgElement('line', { class: tick === 0 ? 'axis' : 'grid', x1: margin.left, x2: width - margin.right, y1: y, y2: y }));
        svg.append(svgElement('text', { x: margin.left - 8, y: y + 4, 'text-anchor': 'end' }, compact(value)));
    }

    points.forEach((point, index) => {
        const center = margin.left + slot * index + slot / 2;
        const baseline = yOf(0);
        [[point.incoming, 'bar-1', center - columnWidth - 1], [point.outgoing, 'bar-2', center + 1]].forEach(([value, cls, x]) => {
            if (value > 0) {
                const top = yOf(value);
                svg.append(svgElement('path', { class: cls, d: columnPath(x, top, columnWidth, Math.max(baseline - top, 1)) }));
            }
        });
        svg.append(svgElement('text', { x: center, y: height - 8, 'text-anchor': 'middle' }, point.label));
        const hit = svgElement('rect', { class: 'hit', x: center - slot / 2, y: margin.top, width: slot, height: plotHeight });
        svg.insertBefore(hit, svg.firstChild);
        const overlay = svgElement('rect', { x: center - slot / 2, y: margin.top, width: slot, height: plotHeight, fill: 'transparent' });
        svg.append(overlay);
        attachTooltip(container, overlay, [point.label, `Зачисления: ${money.format(point.incoming)} ₽`, `Списания: ${money.format(point.outgoing)} ₽`]);
        overlay.addEventListener('mouseenter', () => hit.style.fill = 'var(--line)');
        overlay.addEventListener('mouseleave', () => hit.style.fill = '');
    });

    const legend = document.createElement('div');
    legend.className = 'legend';
    [['key-1', 'Зачисления'], ['key-2', 'Списания']].forEach(([key, label]) => {
        const item = document.createElement('span');
        const swatch = document.createElement('i');
        swatch.className = key;
        item.append(swatch, label);
        legend.append(item);
    });
    container.replaceChildren(legend, svg);
}

function fillTable(id, headers, rows) {
    const holder = document.getElementById(id);
    if (!holder) {
        return;
    }
    const table = document.createElement('table');
    const headRow = table.createTHead().insertRow();
    headers.forEach((header, index) => {
        const cell = document.createElement('th');
        cell.textContent = header;
        cell.className = index > 0 ? 'num' : '';
        headRow.append(cell);
    });
    const body = table.createTBody();
    rows.forEach((row) => {
        const tableRow = body.insertRow();
        row.forEach((value, index) => {
            const cell = tableRow.insertCell();
            cell.textContent = value;
            cell.className = index > 0 ? 'num' : '';
        });
    });
    holder.replaceChildren(table);
}

async function loadCharts() {
    const root = document.querySelector('[data-charts]');
    const turnover = document.getElementById('chart-turnover');
    let data;
    try {
        const response = await fetch(root.dataset.charts, { headers: { Accept: 'application/json' } });
        if (!response.ok) {
            throw new Error(`Код ответа ${response.status}`);
        }
        data = await response.json();
    } catch (error) {
        const note = document.createElement('p');
        note.className = 'alert alert-error';
        note.textContent = 'Не удалось загрузить данные диаграмм. Обновите страницу.';
        turnover.replaceChildren(note);
        return;
    }

    document.getElementById('balances-title').textContent = data.balancesTitle ?? 'Остатки';

    const render = () => {
        drawColumns(turnover, data.turnover);
        drawBars(document.getElementById('chart-balances'), data.balances, compact, '₽');
        const types = document.getElementById('chart-types');
        if (types) {
            drawBars(types, data.types, (value) => number.format(value), 'сч.');
        }
    };
    render();

    fillTable('table-turnover', ['Месяц', 'Зачисления, ₽', 'Списания, ₽'],
        data.turnover.map((p) => [p.label, money.format(p.incoming), money.format(p.outgoing)]));
    fillTable('table-balances', ['Название', 'Сумма, ₽'], data.balances.map((p) => [p.label, money.format(p.value)]));
    if (data.types) {
        fillTable('table-types', ['Вид счёта', 'Количество'], data.types.map((p) => [p.label, number.format(p.value)]));
    }

    let frame = 0;
    new ResizeObserver(() => {
        cancelAnimationFrame(frame);
        frame = requestAnimationFrame(render);
    }).observe(root);
}

loadCharts();
