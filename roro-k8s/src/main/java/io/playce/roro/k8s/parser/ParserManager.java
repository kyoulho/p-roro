/*
 * Copyright 2023 The playce-roro Project.
 *
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
 *
 * Revision History
 * Author			Date				Description
 * ---------------	----------------	------------
 * Dong-Heon Han    Jul 27, 2023		First Draft.
 */

package io.playce.roro.k8s.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParserManager {
    private final Map<String, Parser> parserMap;

    public Parser getParser(String parserName) {
        Parser parser = parserMap.get(parserName);
        if(parser == null) {
            log.error("Parser not found. name: {}", parserName);
            parser = parserMap.get("dummyParser");
        }
        return parser;
    }
}