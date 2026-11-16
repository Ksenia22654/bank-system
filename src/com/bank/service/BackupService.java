package com.bank.service;

import com.bank.dao.AccountDao;
import com.bank.dao.AccountTypeDao;
import com.bank.dao.BackupDao;
import com.bank.dao.ClientDao;
import com.bank.dao.CurrencyDao;
import com.bank.dao.Database;
import com.bank.dao.LoanDao;
import com.bank.dao.OperationDao;
import com.bank.dao.UserDao;
import com.bank.exception.BankException;
import com.bank.exception.BusinessRuleException;
import com.bank.exception.NotFoundException;
import com.bank.model.BackupInfo;
import com.bank.model.BackupSnapshot;
import com.bank.util.AppConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/** Сохранение данных на сервере в файл (сериализация) и загрузка ранее сохранённых данных. */
public class BackupService {
    private static final String FILE_NAME_PATTERN = "backup-\\d{8}-\\d{6}(-\\d+)?\\.ser";
    private static final List<String> ALLOWED_CLASSES = Arrays.asList(
            "java.util.ArrayList", "java.util.Date", "java.sql.Date", "java.sql.Timestamp",
            "java.math.BigDecimal", "java.math.BigInteger", "java.lang.Number", "java.lang.Integer", "[B");

    private final Database database = Database.getInstance();

    /** Сохраняет снимок всех таблиц в согласованном состоянии и возвращает имя файла. */
    public String save() throws BankException {
        BackupSnapshot snapshot = database.inTransaction(() -> new BackupSnapshot(
                new Timestamp(System.currentTimeMillis()),
                new CurrencyDao().findAll(), new AccountTypeDao().findAll(), new ClientDao().findAll(),
                new UserDao().findAll(), new AccountDao().findAll(), new LoanDao().findAll(), new OperationDao().findAll()));
        File directory = AppConfig.backupDir();
        directory.mkdirs();
        File file = uniqueFile(directory);
        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(file.toPath())))) {
            out.writeObject(snapshot);
        } catch (IOException e) {
            throw new BusinessRuleException("Не удалось записать файл резервной копии", e);
        }
        return file.getName();
    }

    public List<BackupInfo> list() {
        File[] files = AppConfig.backupDir().listFiles((dir, name) -> name.matches(FILE_NAME_PATTERN));
        List<BackupInfo> result = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                result.add(new BackupInfo(file.getName(), new Date(file.lastModified()), file.length()));
            }
        }
        result.sort(Comparator.comparing(BackupInfo::getFileName).reversed());
        return result;
    }

    /** Заменяет текущие данные содержимым файла. При ошибке транзакция откатывается и данные не меняются. */
    public void restore(String fileName) throws BankException {
        File file = resolve(fileName);
        BackupSnapshot snapshot;
        try (InputStream in = new BufferedInputStream(Files.newInputStream(file.toPath()));
             ObjectInputStream objects = new SnapshotInputStream(in)) {
            snapshot = (BackupSnapshot) objects.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new BusinessRuleException("Файл резервной копии повреждён или имеет неверный формат", e);
        }
        database.inTransaction(() -> {
            new BackupDao().replaceAll(snapshot);
            return null;
        });
    }

    public void delete(String fileName) throws BankException {
        if (!resolve(fileName).delete()) {
            throw new BusinessRuleException("Не удалось удалить файл");
        }
    }

    private static File uniqueFile(File directory) {
        String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        File file = new File(directory, "backup-" + stamp + ".ser");
        for (int i = 2; file.exists(); i++) {
            file = new File(directory, "backup-" + stamp + "-" + i + ".ser");
        }
        return file;
    }

    /** Имя файла проверяется по шаблону: выйти за пределы каталога копий невозможно. */
    private static File resolve(String fileName) throws NotFoundException {
        if (fileName == null || !fileName.matches(FILE_NAME_PATTERN)) {
            throw new NotFoundException("Файл резервной копии не найден");
        }
        File file = new File(AppConfig.backupDir(), fileName);
        if (!file.isFile()) {
            throw new NotFoundException("Файл резервной копии не найден");
        }
        return file;
    }

    /** Десериализация только разрешённых классов модели. */
    private static final class SnapshotInputStream extends ObjectInputStream {
        SnapshotInputStream(InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass description) throws IOException, ClassNotFoundException {
            String name = description.getName();
            if (!name.startsWith("com.bank.model.") && !ALLOWED_CLASSES.contains(name)) {
                throw new InvalidClassException(name, "Класс не разрешён для загрузки");
            }
            return Class.forName(name, false, BackupService.class.getClassLoader());
        }
    }
}
