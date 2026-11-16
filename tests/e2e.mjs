const BASE = 'http://localhost:8080/bank/app';
let failed = 0, passed = 0;
const check = (name, ok, extra = '') => { if (ok) { passed++; } else { failed++; console.log(`FAIL ${name} ${extra}`); } };

class Session {
  cookie = ''; csrf = ''; last = BASE + '/home';
  async req(path, opts = {}) {
    const r = await fetch(BASE + path, { redirect: 'manual', ...opts, headers: { Cookie: this.cookie, ...(opts.headers || {}) } });
    const sc = r.headers.get('set-cookie'); if (sc) this.cookie = sc.split(';')[0];
    const body = await r.text();
    const m = body.match(/name="csrf" value="([^"]+)"/); if (m) this.csrf = m[1];
    return { status: r.status, body, location: r.headers.get('location'), headers: r.headers };
  }
  get(p) { this.last = BASE + p; return this.req(p); }
  async post(p, data = {}, csrf = true) {
    const form = new URLSearchParams({ ...(csrf ? { csrf: this.csrf } : {}), ...data });
    return this.req(p, { method: 'POST', body: form, headers: { 'Content-Type': 'application/x-www-form-urlencoded', Referer: this.last } });
  }
  async follow(r) { return r.location ? this.get(r.location.replace('/bank/app', '')) : r; }
  async login(l, p) { await this.get('/login'); const r = await this.post('/login', { login: l, password: p }); if (r.status === 302) await this.get('/home'); return r; }
}
const num = s => Number(s.replace(/[\s ]/g, '').replace(',', '.'));
const balanceOf = async (s, id) => { const b = (await s.get(`/accounts/view?id=${id}`)).body; const m = b.match(/class="strong">([\d\s ,]+)/); return num(m[1]); };

const admin = new Session();
let r = await admin.login('admin', 'wrong');
check('wrong password stays on login', r.status === 200 && r.body.includes('Неверный логин'));
r = await admin.login('admin', 'admin123');
check('admin login', r.status === 302, r.status);

for (const p of ['/home', '/analytics', '/clients', '/clients/view?id=1', '/clients/new', '/clients/edit?id=1', '/accounts', '/accounts/new', '/accounts/view?id=1',
  '/operations', '/operations/deposit', '/operations/withdraw', '/operations/transfer', '/loans', '/loans/apply', '/loans/edit?id=2', '/currencies', '/currencies?id=1',
  '/account-types', '/users', '/users/new', '/users/edit?id=2', '/reports', '/backups', '/profile']) {
  r = await admin.get(p);
  check(`GET ${p} 200`, r.status === 200, r.status);
  check(`GET ${p} no raw EL`, !r.body.includes('${'));
  check(`GET ${p} no glued attrs`, !/requiredautocomplete|required(pattern|title|min|max|step)=/.test(r.body));
  check(`GET ${p} menu xslt`, r.body.includes('Главное меню') && r.body.includes('Резервные копии'));
}
check('active menu item', (await admin.get('/clients')).body.includes('class="active" aria-current="page"') );

// Клиенты: серверная проверка и CRUD
r = await admin.get('/clients/new');
r = await admin.post('/clients/save', { lastName: '', firstName: 'А', birthDate: '2999-01-01', passportNo: 'bad', phone: 'x', address: '' });
check('server validation', r.status === 200 && r.body.includes('Заполните поле «Фамилия»') && r.body.includes('Паспорт: от 6') && r.body.includes('в будущем'));
r = await admin.post('/clients/save', { lastName: 'Иванов', firstName: 'Дубль', birthDate: '1990-01-01', passportNo: '4510 123456', phone: '+7 900 000-00-00', address: 'Адрес' });
check('duplicate passport', r.body.includes('уже зарегистрирован'));
r = await admin.post('/clients/save', { lastName: '<script>alert(1)</script>', firstName: 'Тест', birthDate: '1990-01-01', passportNo: '9999 000001', phone: '+7 900 000-00-00', address: 'г. Тест' });
check('client created', r.status === 302, r.status + r.body.slice(0, 200));
const newClient = Number(r.location.match(/id=(\d+)/)[1]);
r = await admin.follow(r);
check('cyrillic+flash', r.body.includes('Данные клиента сохранены'));
check('xss escaped', r.body.includes('&lt;script&gt;alert(1)') && !r.body.includes('<script>alert(1)'));
r = await admin.post('/clients/save', { id: newClient, lastName: 'Тестовый', firstName: 'Тест', birthDate: '1990-01-01', passportNo: '9999 000001', phone: '+7 900 000-00-00', address: 'г. Тест' });
check('client edited', r.status === 302 && (await admin.get(`/clients/view?id=${newClient}`)).body.includes('Тестовый Тест'));
check('search', (await admin.get('/clients?q=' + encodeURIComponent('тестов'))).body.includes('Тестовый'));

// Счета
r = await admin.post('/accounts/open', { clientId: newClient, typeId: 2, currencyId: 2 });
check('open account', r.status === 302, r.status);
const usd = Number(r.location.match(/id=(\d+)/)[1]);
const usdNo = (await admin.get(`/accounts/view?id=${usd}`)).body.match(/Счёт (\d{20})/)[1];
r = await admin.post('/clients/delete', { id: newClient });
check('client with account not deletable', (await admin.follow(r)).body.includes('есть счета'));

// Операции
r = await admin.post('/operations/deposit', { accountId: usd, amount: '100.50' });
check('deposit', r.status === 302 && await balanceOf(admin, usd) === 100.5);
r = await admin.post('/operations/withdraw', { accountId: usd, amount: '1000' });
check('overdraft refused', r.status === 200 && r.body.includes('Недостаточно средств') && await balanceOf(admin, usd) === 100.5);
r = await admin.post('/operations/deposit', { accountId: usd, amount: '-5' });
check('negative refused', r.status === 200 && r.body.includes('больше нуля'));
r = await admin.post('/operations/deposit', { accountId: usd, amount: '1.234' });
check('3 decimals refused', r.status === 200 && r.body.includes('двух знаков'));
const beforeRub = await balanceOf(admin, 7);
r = await admin.post('/operations/transfer', { fromId: 7, toNumber: usdNo, amount: '8000' });
check('cross-currency transfer', r.status === 302 && await balanceOf(admin, usd) === 200.5 && await balanceOf(admin, 7) === beforeRub - 8000, r.status);
r = await admin.post('/operations/transfer', { fromId: 7, toNumber: '40817810000000000007', amount: '1' });
check('self transfer refused', r.body.includes('совпадают'));
r = await admin.post('/accounts/close', { id: usd });
check('close nonzero refused', (await admin.follow(r)).body.includes('нулевым остатком'));

// CSRF
r = await admin.post('/clients/delete', { id: 6 }, false);
check('csrf required', r.status === 403, r.status);

// Проценты
await admin.get('/operations');
const sav = await balanceOf(admin, 2);
r = await admin.post('/operations/interest');
const flash = (await admin.follow(r)).body;
check('interest accrued', flash.includes('Проценты начислены'));
check('interest amount', await balanceOf(admin, 2) === Math.round((sav + sav * 6 / 1200) * 100) / 100, await balanceOf(admin, 2));
r = await admin.post('/operations/interest');
check('interest idempotent', (await admin.follow(r)).body.includes('уже начислены'));

// Роль пользователя
const user = new Session();
await user.login('ivanov', 'user123');
r = await user.get('/home');
check('user sees own', r.status === 200 && r.body.includes('Мои счета') && !r.body.includes('Резервные копии'));
for (const p of ['/clients', '/users', '/backups', '/currencies', '/operations/deposit']) check(`user 403 ${p}`, (await user.get(p)).status === 403);
check('user foreign account 403', (await user.get('/accounts/view?id=3')).status === 403);
check('user own account', (await user.get('/accounts/view?id=1')).status === 200);
check('user only own accounts', !(await user.get('/accounts')).body.includes('40817810000000000003'));
await user.get('/accounts');
r = await user.post('/operations/transfer', { fromId: 3, toNumber: '40817810000000000001', amount: '10' });
check('user foreign transfer 403', r.status === 403, r.status);
check('user foreign statement 403', (await user.get('/reports/download?type=statement&id=3')).status === 403);
check('user clients report 403', (await user.get('/reports/download?type=clients')).status === 403);
await user.get('/accounts');
r = await user.post('/accounts/open', { clientId: 3, typeId: 1, currencyId: 1 });
const own = Number(r.location.match(/id=(\d+)/)[1]);
check('user opens only for self', (await admin.get(`/accounts/view?id=${own}`)).body.includes('Иванов Иван'));
await user.get('/loans');
r = await user.post('/loans/pay', { id: 1 });
check('user foreign loan 403', r.status === 403, r.status);

// Кредит
await user.get('/loans/apply');
r = await user.post('/loans/apply', { accountId: 1, amount: '12000', termMonths: 3 });
check('loan applied', r.status === 302, r.status);
const loans = (await admin.get('/loans')).body;
const loanId = Number(loans.match(/<td>(\d+)<\/td>\s*<td>Иванов Иван/)[1]);
const accBefore = await balanceOf(admin, 1);
await admin.get('/loans');
r = await admin.post('/loans/approve', { id: loanId });
check('loan approved', r.status === 302 && await balanceOf(admin, 1) === accBefore + 12000);
await admin.get('/loans');
r = await admin.post('/loans/approve', { id: loanId });
check('double approve refused', (await admin.follow(r)).body.includes('уже рассмотрена') && await balanceOf(admin, 1) === accBefore + 12000);
for (let i = 0; i < 3; i++) { await user.get('/loans'); r = await user.post('/loans/pay', { id: loanId }); check('loan pay', r.status === 302); }
check('loan closed after term', /<td>Погашен<\/td>/.test((await user.get('/loans')).body));
await user.get('/loans');
r = await user.post('/loans/pay', { id: loanId });
check('pay closed refused', (await user.follow(r)).body.includes('действующему'));

// Отчёты
for (const t of ['clients', 'accounts', 'operations', 'loans', 'statement&id=1']) {
  const res = await admin.get(`/reports/download?type=${t}`);
  check(`report ${t}`, res.status === 200 && res.headers.get('content-disposition').includes('.txt') && res.body.includes('Сформирован:') , res.status);
}
console.log((await admin.get('/reports/download?type=loans')).body);

// Диаграммы
r = await admin.get('/analytics/data');
const chart = JSON.parse(r.body);
check('chart json', chart.turnover.length === 6 && chart.balances.length === 3 && chart.types.length > 0);
const uc = JSON.parse((await user.get('/analytics/data')).body);
check('user chart own only', !uc.types && uc.balances.every(b => !b.label.includes('0003')));

// Параллельность: встречные переводы и гонка за остаток
const a = 1, b = 2, noA = '40817810000000000001', noB = '40817810000000000002';
await admin.get('/operations/transfer');
const startA = await balanceOf(admin, a), startB = await balanceOf(admin, b);
const t0 = Date.now();
const results = await Promise.all(Array.from({ length: 60 }, (_, i) => i % 2
  ? admin.post('/operations/transfer', { fromId: a, toNumber: noB, amount: '7.00' })
  : admin.post('/operations/transfer', { fromId: b, toNumber: noA, amount: '5.00' })));
check('parallel all ok', results.every(x => x.status === 302), JSON.stringify(results.map(x => x.status).filter(s => s !== 302)));
const endA = await balanceOf(admin, a), endB = await balanceOf(admin, b);
check('parallel exact balances', Math.abs(endA - (startA - 30 * 7 + 30 * 5)) < 0.001 && Math.abs(endB - (startB + 30 * 7 - 30 * 5)) < 0.001, `${endA} ${endB}`);
console.log(`60 параллельных переводов: ${Date.now() - t0} мс`);
r = await admin.post('/accounts/open', { clientId: 6, typeId: 1, currencyId: 1 });
const race = Number(r.location.match(/id=(\d+)/)[1]);
await admin.get('/operations/deposit');
await admin.post('/operations/deposit', { accountId: race, amount: '100' });
await admin.get('/operations/withdraw');
const rr = await Promise.all(Array.from({ length: 20 }, () => admin.post('/operations/withdraw', { accountId: race, amount: '30' })));
check('race: exactly 3 succeed', rr.filter(x => x.status === 302).length === 3 && await balanceOf(admin, race) === 10, rr.filter(x => x.status === 302).length);

// Резервные копии
await admin.get('/backups');
r = await admin.post('/backups/save');
check('backup saved', r.status === 302);
let page = (await admin.follow(r)).body;
const file = page.match(/backup-\d{8}-\d{6}\.ser/)[0];
const saved = await balanceOf(admin, a);
await admin.get('/clients/new');
r = await admin.post('/clients/save', { lastName: 'ПослеКопии', firstName: 'Тест', birthDate: '1990-01-01', passportNo: '9999 000002', phone: '+7 900 000-00-00', address: 'г. Тест' });
await admin.get('/operations/deposit');
await admin.post('/operations/deposit', { accountId: a, amount: '1' });
check('changed', await balanceOf(admin, a) === saved + 1);
await admin.get('/backups');
r = await admin.post('/backups/restore', { file });
check('restored', (await admin.follow(r)).body.includes('Данные загружены'));
check('restored balance', await balanceOf(admin, a) === saved);
check('restored clients', !(await admin.get('/clients')).body.includes('ПослеКопии'));
await admin.get('/clients/new');
r = await admin.post('/clients/save', { lastName: 'ПослеВосст', firstName: 'Тест', birthDate: '1990-01-01', passportNo: '9999 000003', phone: '+7 900 000-00-00', address: 'г. Тест' });
check('identity works after restore', r.status === 302, r.status + r.body.slice(0, 300));
await admin.get('/backups');
r = await admin.post('/backups/restore', { file: '../../conf/bank.properties' });
check('traversal refused', r.status === 404, r.status);

// Пользователи
await admin.get('/users');
r = await admin.post('/users/delete', { id: 1 });
check('self delete refused', (await admin.follow(r)).body.includes('собственную'));
await admin.get('/users/new');
r = await admin.post('/users/save', { login: 'x', roleCode: 'USER' });
check('user validation', r.status === 200 && r.body.includes('Логин: от 3') && r.body.includes('Заполните поле «Пароль»') && r.body.includes('связан с клиентом'));
await admin.get('/users/new');
r = await admin.post('/users/save', { login: 'popova', password: 'secret1', roleCode: 'USER', clientId: 6, active: 'on' });
check('user created', r.status === 302, r.status);
const p = new Session();
check('new user login', (await p.login('popova', 'secret1')).status === 302);
await p.get('/profile');
r = await p.post('/profile', { current: 'bad', next: 'secret2', repeat: 'secret2' });
check('wrong current password', r.body.includes('указан неверно'));
await p.get('/profile');
r = await p.post('/profile', { current: 'secret1', next: 'secret2', repeat: 'secret2' });
check('password changed', r.status === 302 && (await new Session().login('popova', 'secret2')).status === 302);
const users = (await admin.get('/users')).body;
const pid = users.match(/popova<\/td>[^]*?users\/edit\?id=(\d+)/);
const popovaId = Number(pid[1]);
await admin.get('/users');
r = await admin.post('/users/save', { id: popovaId, login: 'popova', roleCode: 'USER', clientId: 6 });
check('block user', r.status === 302);
check('blocked session ends at once', (await p.get('/home')).status === 302);
check('blocked cannot login', (await new Session().login('popova', 'secret2')).status === 200);

// Ошибки и выход
check('404 page', (await admin.get('/nope')).body.includes('Страница не найдена'));
check('missing record 404', (await admin.get('/clients/view?id=99999')).status === 404);
await admin.get('/home');
r = await admin.post('/logout');
check('logout', r.status === 302 && (await admin.get('/home')).status === 302);

console.log(`\nПройдено: ${passed}, ошибок: ${failed}`);
process.exit(failed ? 1 : 0);
