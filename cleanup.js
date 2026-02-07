const fs = require('fs');
const path = require('path');

const targets = [
    'c:/Users/Youcode/Desktop/briefs/TraceAndTrust/src/main/java/c:',
    'c:/Users/Youcode/Desktop/briefs/TraceAndTrust/src/main/java/org/usermanagement/traceandtrust/service/c:',
    'c:/Users/Youcode/Desktop/briefs/TraceAndTrust/src/main/java/org/usermanagement/traceandtrust/dto/c:'
];

targets.forEach(dir => {
    if (fs.existsSync(dir)) {
        console.log(`Deleting ${dir}...`);
        fs.rmSync(dir, { recursive: true, force: true });
        console.log('Done.');
    } else {
        console.log(`Path ${dir} not found.`);
    }
});
