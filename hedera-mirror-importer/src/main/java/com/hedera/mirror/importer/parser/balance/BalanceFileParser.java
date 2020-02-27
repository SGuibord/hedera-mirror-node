package com.hedera.mirror.importer.parser.balance;

/*-
 * ‌
 * Hedera Mirror Node
 * ​
 * Copyright (C) 2019 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import com.google.common.base.Stopwatch;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Named;

import com.hedera.mirror.importer.parser.FileWatcher;
import com.hedera.mirror.importer.util.Utility;

@Named
public class BalanceFileParser extends FileWatcher {

    private final FileProcessor balanceFileProcessor =
            (balanceFileName, inputStream) ->
                    new AccountBalancesFileLoader((BalanceParserProperties) parserProperties, balanceFileName, inputStream)
                            .loadAccountBalances();

    public BalanceFileParser(BalanceParserProperties parserProperties) {
        super(parserProperties);
    }

    @Override
    public void onCreate() {
        processLastBalanceFile();
        listAndProcessAllFiles(balanceFileProcessor);
    }

    @Override
    public boolean isDataFile(String filename) {
        return Utility.isBalanceFile(filename);
    }

    // Find all files in path. Return the greatest file name.
    private File getLatestBalanceFile() {
        File lastFile = null;

        File balanceFilePath = parserProperties.getValidPath().toFile();
        List<String> balancefiles = new ArrayList<>();
        for (File balanceFile : balanceFilePath.listFiles()) {
            if (balanceFile.getName().toString().endsWith(".csv")) {
                balancefiles.add(balanceFile.getName());
            }
        }
        if (balancefiles.size() != 0) {
            Collections.sort(balancefiles);
            lastFile = parserProperties.getValidPath().resolve(balancefiles.get(balancefiles.size() - 1)).toFile();
        }
        return lastFile;
    }

    private void processLastBalanceFile() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            File balanceFile = getLatestBalanceFile();
            if (balanceFile == null) {
                return;
            }
            log.debug("Processing last balance file {}", balanceFile);
            processAllFiles(List.of(balanceFile), balanceFileProcessor);
        } catch (Exception e) {
            log.error("Error processing balances files after {}", stopwatch, e);
        }
    }
}
