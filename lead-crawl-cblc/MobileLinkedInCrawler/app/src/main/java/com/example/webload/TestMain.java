package com.example.webload;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class TestMain extends AppCompatActivity {


    static int count=0;
    static String arr[] = {
            "https://www.bing.com/search?q=https://in.linkedin.com/in/naman-sharma-ba571221",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/leswhiting&sa=U&ved=2ahUKEwiQ_aeCkNLpAhVZzDgGHRzqDL8QFjAAegQIBRAB&usg=AOvVaw3M5KjFnzqV8ZgY1EoldxO0",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/patrickheffron&sa=U&ved=2ahUKEwjV3emGkNLpAhViyDgGHYVWAvYQFjAAegQIABAB&usg=AOvVaw2IiJ6iiIFxd1-nyuc1OOc8",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/rachel-bachmann-9312789&sa=U&ved=2ahUKEwie1KKLkNLpAhWyzDgGHfxnCL0QFjAAegQIARAB&usg=AOvVaw1pAFHZUw1N0BjmQseEGQZ9",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/gary-dephoure&sa=U&ved=2ahUKEwjW-bmPkNLpAhUjwzgGHY70DlYQFjAAegQIBRAB&usg=AOvVaw0ciwxi4LCaRhuhLaXWxtYi",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://www.linkedin.com/in/john-wunderlich-9396a27&sa=U&ved=2ahUKEwiYkdiTkNLpAhWE4jgGHbTTCn0QFjAAegQIABAB&usg=AOvVaw1715OWXj1Gp_gu9OOGBQyq",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/heidischwende&sa=U&ved=2ahUKEwiTytyXkNLpAhXPzzgGHWdqBwYQFjAAegQIBRAB&usg=AOvVaw1TZ0oGaZPfWkPZOJtILFfc",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/murray-thomas-32b5745&sa=U&ved=2ahUKEwiEr_abkNLpAhUbyzgGHfc-CmAQFjAAegQIABAB&usg=AOvVaw0dRuXpp90wBFQfqY5lWbmn",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/gordon-dyer-23b61015&sa=U&ved=2ahUKEwiskYigkNLpAhWhzzgGHVcuCAgQFjAAegQIARAB&usg=AOvVaw2TjmynoWXBvQf5rWZ7OMEf",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/skerkmann&sa=U&ved=2ahUKEwjd_ZmkkNLpAhU4zTgGHaAgCiUQFjAAegQIABAB&usg=AOvVaw2tkdYvRpRR816COu-Ng7JA",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/alon-zvi-goldberg-b46a9b5b&sa=U&ved=2ahUKEwjq9ayokNLpAhVbzDgGHRBxBscQFjAAegQIABAB&usg=AOvVaw3mCb2C-uDMUMd59Wrd7Znu",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/pierrecourchesne&sa=U&ved=2ahUKEwjd5LeskNLpAhXPwjgGHVI2AcIQFjAAegQIABAB&usg=AOvVaw1GCvFqeTaiTJFeMhNPmh9C",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/alain-marceau-80766829/%257Bcountry%253Dus%252C%2Blanguage%253Den%257D%3Ftrk%3Dpeople-guest_profile-result-card_result-card_full-click&sa=U&ved=2ahUKEwixicOwkNLpAhWryzgGHbHzBOYQFjAAegQIARAB&usg=AOvVaw3ANuqUEAQxIo8qrR0B_mab",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://in.linkedin.com/in/manukumarjain&sa=U&ved=2ahUKEwikoNG0kNLpAhU8zzgGHfWgA64QFjAAegQIABAB&usg=AOvVaw00k_4zrjex4SHwYfMjxBer",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/petercauley&sa=U&ved=2ahUKEwjJ-va4kNLpAhVV4jgGHT63Cb8QFjAAegQIABAB&usg=AOvVaw2SW4FaJbUN8eksoqgxu2HL",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://www.linkedin.com/company/cfo&sa=U&ved=2ahUKEwiv_Ii9kNLpAhW8yzgGHYcjAewQFjAAegQIAhAB&usg=AOvVaw1Z04Syf6Y4nb_YqNsoqf9o",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/abannis&sa=U&ved=2ahUKEwiri43BkNLpAhUmwzgGHUtMBQYQFjAAegQIBBAB&usg=AOvVaw0N0va7VPjp4oOqBlfk8bGg",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/petelow&sa=U&ved=2ahUKEwjElKbFkNLpAhX3zzgGHQnfCEcQFjAAegQIBRAB&usg=AOvVaw1TebQejT_Tiv-HJEXQMG7M",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/trevoroseen&sa=U&ved=2ahUKEwjuhM7JkNLpAhUNyDgGHQkFAf0QFjAAegQIBBAB&usg=AOvVaw0TM45nIb22fWvvEErrD56R",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/stevemcnamara&sa=U&ved=2ahUKEwiFo-XNkNLpAhVFyDgGHakIBKAQFjAAegQIBBAB&usg=AOvVaw0n0-6b7atekKbxkFB0HJZd",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/maninagappan&sa=U&ved=2ahUKEwjrqv7RkNLpAhVEwjgGHa0kDi0QFjAAegQIABAB&usg=AOvVaw10UIy4JcqkRZmUN92uRn7t",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/rekhamayya&sa=U&ved=2ahUKEwjZ3JPWkNLpAhVr4zgGHYYYCn0QFjAAegQIARAB&usg=AOvVaw2RnMYTZDgafLSgXb-qVQQe",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/scottedmonds&sa=U&ved=2ahUKEwjTk4zakNLpAhUh4jgGHfAyDkYQFjAAegQIAxAB&usg=AOvVaw0RNbWkh36lqoxtEzj1VhI7",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/patrickcormier&sa=U&ved=2ahUKEwj2ounekNLpAhXuyDgGHRa0BqMQFjAAegQIBRAB&usg=AOvVaw1hB2Kp7KesA8rCMpcK-gH4",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/peterkozik&sa=U&ved=2ahUKEwiHhu7ikNLpAhXByTgGHYtyCskQFjAAegQIARAB&usg=AOvVaw0hL1Xoz81x6N7sYgQS7zDR",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/bryantholt&sa=U&ved=2ahUKEwi34PbmkNLpAhWoxzgGHQJOA6kQFjAAegQIBhAB&usg=AOvVaw2yvr2oxckmhkbQB-3ymw4E",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/daniel-forest-292b3a1&sa=U&ved=2ahUKEwj2tInrkNLpAhXqzjgGHQcMCZkQFjAAegQIARAB&usg=AOvVaw0nrej9d4oQBZQnvDrHR3tZ",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/jmaudet&sa=U&ved=2ahUKEwin-LLvkNLpAhW5yDgGHZedBGEQFjAAegQIARAB&usg=AOvVaw3ocnW-bSPM9GL-NW21_6DQ",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/daniel-bastien-5174351&sa=U&ved=2ahUKEwiSkd7zkNLpAhUuxjgGHcfpAg0QFjAAegQIABAB&usg=AOvVaw01jxheKCQj-Q0GygSBHBL9",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/ken-grant-8bb14448&sa=U&ved=2ahUKEwik45H4kNLpAhWkwjgGHbSNDC0QFjAAegQIABAB&usg=AOvVaw2eviiyMeSm2PuG87vNdHqX",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/henri-besnier-5a4498&sa=U&ved=2ahUKEwjwxpn8kNLpAhVozTgGHd9pDRYQFjAAegQIABAB&usg=AOvVaw1vYxm9LkcuQJ3Fkg_zhWBA",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/timothyjwu&sa=U&ved=2ahUKEwi-l6eAkdLpAhVGzDgGHfgrDmcQFjAAegQIABAB&usg=AOvVaw0CCtqGLzMGjgi6Vn9T5zrt",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/sachasawaya&sa=U&ved=2ahUKEwj5iLSEkdLpAhWGxzgGHaEEBhIQFjAAegQIBhAB&usg=AOvVaw1dKQV7Btv8PEeVF9UqbK8D",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/aaronakerman&sa=U&ved=2ahUKEwiks9uIkdLpAhVozzgGHUuxAbkQFjAAegQIBBAB&usg=AOvVaw1oL-n76zmFWbfhAw3XWgv8",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/shanemgrennan&sa=U&ved=2ahUKEwj40eeMkdLpAhUSxzgGHeRYCfsQFjAAegQIARAB&usg=AOvVaw2PGH7hwcAvpOm4K4DA0KMX",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/anatolii-shkliaruk&sa=U&ved=2ahUKEwjNxIKRkdLpAhVLzjgGHWTxBEUQFjAAegQIABAB&usg=AOvVaw2G76xDw7aR7ssEU-Ek73oi",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/carlosfigueiredonetto&sa=U&ved=2ahUKEwj1vJuVkdLpAhUnyzgGHfCjDuUQFjAAegQIARAB&usg=AOvVaw2jfYUi-ZvggJAat-4I9gEX",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/bryan-rocco-31308849&sa=U&ved=2ahUKEwjizL6ZkdLpAhWWyzgGHS_UASUQFjAAegQIARAB&usg=AOvVaw043pwPQN_VXLxCe_wbTIRZ",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/emily-chen-799b4941&sa=U&ved=2ahUKEwiht8udkdLpAhWu4jgGHfWcDRsQFjAAegQIABAB&usg=AOvVaw0EqFv62AWe3JU0fCgTJa62",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/dominicjaar&sa=U&ved=2ahUKEwi06dihkdLpAhVXxTgGHb2ED2wQFjAAegQIARAB&usg=AOvVaw3Sqr4hEWxpuSBOD52EzAbu",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://in.linkedin.com/in/karim-gillani-8849708&sa=U&ved=2ahUKEwjWyfilkdLpAhVjwzgGHWzQATsQFjAAegQIABAB&usg=AOvVaw37cFLEg0zHdb_ZqfmoyMSq",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/daniel-lee-966b1620&sa=U&ved=2ahUKEwi45KeqkdLpAhWjyzgGHQO-Aj8QFjAAegQIABAB&usg=AOvVaw1aTwMyey5Dn8v_owFF-sNt",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://bd.linkedin.com/in/mashfiq-ahmed-sharif-3313b928&sa=U&ved=2ahUKEwi02qGukdLpAhX2zDgGHc85BlUQFjAAegQIABAB&usg=AOvVaw3Gbx_R58UgNs-DSWh4vLWW",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/matthewspoke&sa=U&ved=2ahUKEwjEt7uykdLpAhXj4zgGHY_KDywQFjAAegQIAxAB&usg=AOvVaw1zQK0I6QSHMcwx1G1VjzAk",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/laurent-binda-49b60863&sa=U&ved=2ahUKEwjZssm2kdLpAhUP4zgGHfJjA4cQFjAAegQIABAB&usg=AOvVaw3IhFSB7D5j4WDQpxG1z-Zj",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/rmagani&sa=U&ved=2ahUKEwiW5fG6kdLpAhUlwzgGHVfwCgQQFjAAegQIABAB&usg=AOvVaw23-VsZz9k6L23Ds3rAaHQh",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/jgregoryphillips&sa=U&ved=2ahUKEwid1va-kdLpAhWDzTgGHbDyAdIQFjAAegQIABAB&usg=AOvVaw3H87Gt3L--sQ9mvcK0_fJR",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/svettese&sa=U&ved=2ahUKEwiakvjCkdLpAhWZwzgGHQDpDLIQFjAAegQIABAB&usg=AOvVaw3-VLOQs4suZb4s4bcLjumW",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/lukegass&sa=U&ved=2ahUKEwil9-_GkdLpAhVdyzgGHYDtBqsQFjAAegQIARAB&usg=AOvVaw2ndJXacwpHveRSJ0xPNHLZ",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/patrickbertrand&sa=U&ved=2ahUKEwiylILLkdLpAhW3wzgGHbmSDisQFjAAegQIARAB&usg=AOvVaw3YoRV8mYHk7fNOBzEu796G",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://www.linkedin.com/in/dietmartietz&sa=U&ved=2ahUKEwj9sJPPkdLpAhXl4zgGHQPkDVMQFjAAegQIBBAB&usg=AOvVaw0CVuhO0icx8tFvP-rCNU-d",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/jauyeung&sa=U&ved=2ahUKEwjT7KTTkdLpAhXxzzgGHT8XA8YQFjAAegQIABAB&usg=AOvVaw3P5Kd8m2_fZ9j0UvV_2FXQ",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://au.linkedin.com/in/matthew-saunders-03461547&sa=U&ved=2ahUKEwia0MHXkdLpAhVmzjgGHRauCyIQFjAAegQIABAB&usg=AOvVaw2hnak9p9PskH1phzd6JPy3",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/mike-cook-79a4693&sa=U&ved=2ahUKEwiDucXbkdLpAhV4yTgGHTBKCJMQFjAAegQIARAB&usg=AOvVaw25nTd7PHndZn6B7uCzojyu",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/stevenmilstein&sa=U&ved=2ahUKEwjfmsPfkdLpAhVhyTgGHXSyCbAQFjAAegQIABAB&usg=AOvVaw1NGkw7mPWnb7kS8Ss6ZkEX",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/jonathanfranktoronto&sa=U&ved=2ahUKEwi7wd_jkdLpAhVSxjgGHfMIAr8QFjAAegQIAhAB&usg=AOvVaw1I5SSWc6JibDpcRSxDj7NC",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/serge-sinclair-8b749915b&sa=U&ved=2ahUKEwixo_3nkdLpAhUMyzgGHdv4DkMQFjAAegQIABAB&usg=AOvVaw0Mt9hZfsBC75RZYjLRtUCU",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/nikolovadesislava&sa=U&ved=2ahUKEwiOiYvskdLpAhXFzjgGHXtDArQQFjAAegQIABAB&usg=AOvVaw3m0lZ71k8N6-A72-95xtOh",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/peter-kinash-cpa-ca-icd-d-4040217&sa=U&ved=2ahUKEwiC1aLwkdLpAhVAyzgGHVqBDYkQFjAAegQIABAB&usg=AOvVaw0XI6lmfUFnHaZsWDz_pny1",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://www.linkedin.com/signup/public-profile-join%3FvieweeVanityName%3Dcsmith06%26trk%3Dpublic_profile_top-card-primary-button-join-to-connect&sa=U&ved=2ahUKEwiL1Ln0kdLpAhXPyjgGHWhKBUMQFjAAegQIABAB&usg=AOvVaw2Y8C1pah3Fr6QwU0LCgk3D",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/nigel-fenty&sa=U&ved=2ahUKEwig-7P4kdLpAhWkzjgGHa-kAEYQFjAAegQIBRAB&usg=AOvVaw0JSIjvtdbuKqH7wnn1WSiM",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/lucie-boudreau-1a152878&sa=U&ved=2ahUKEwiulsL8kdLpAhVjxzgGHeHnC2YQFjAAegQIABAB&usg=AOvVaw2ihA-8QEMywL5-lJWdc2yh",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/joan-smith-a909a443&sa=U&ved=2ahUKEwjh8LmAktLpAhXbwTgGHfXSDqcQFjAAegQIABAB&usg=AOvVaw1h5xnjp2Yux6-zN8Dh7Uix",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://www.linkedin.com/chatin/wnc/in/nazim-somani-cpa-cma-canada-7a686913%3Ftrk%3Dpeople_also_view_1&sa=U&ved=2ahUKEwjX2NCEktLpAhXJxzgGHbKIDIgQFjAAegQIARAB&usg=AOvVaw3x-ZY77DAl_s7m1aT21tZh",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/maciek-hunek-08454714&sa=U&ved=2ahUKEwjAldyIktLpAhWkyzgGHbREBakQFjAAegQIARAB&usg=AOvVaw0EDrQvCADgoUVEgI08CJKL",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/claude-thibault-cpa-ca-cf-cbv-mba-icd-d-b7978a%3Ftrk%3Dpublic_profile_browsemap_profile-result-card_result-card_full-click&sa=U&ved=2ahUKEwiM2_CMktLpAhVRxzgGHeLHAGEQFjAAegQIABAB&usg=AOvVaw2vNQCkv_kn0pmOKjaBkl0E",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://in.linkedin.com/in/dilip-jose-13b9961b&sa=U&ved=2ahUKEwjTyZ-RktLpAhURxzgGHZ3TAyIQFjAAegQIARAB&usg=AOvVaw21nBnv4voNNfv0qGYkObUh",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/mark-millar-cpa-c-a-ab43b89&sa=U&ved=2ahUKEwjamrmVktLpAhXXyDgGHRBWCK4QFjAAegQIARAB&usg=AOvVaw0rc7bwKl9QULUqsQHHdc9s",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/ryanbholm&sa=U&ved=2ahUKEwjPiIaaktLpAhVvyzgGHfYICsEQFjAAegQIARAB&usg=AOvVaw2-fLeBNyZ_WPINzC_8Xpa7",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/brice-scheschuk-cpa-ca-095721a&sa=U&ved=2ahUKEwjGj5qektLpAhXOzzgGHe-CB_gQFjAAegQIABAB&usg=AOvVaw0TD6jNbAa9LyXU85r57TE1",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/anish-mukherjee-94a632b0&sa=U&ved=2ahUKEwjYy6uiktLpAhVjzTgGHcacC_4QFjAAegQIABAB&usg=AOvVaw0OGseQotYxZDdaRTxxhNt6",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/sheldon-gardiner-05b75375&sa=U&ved=2ahUKEwjstLamktLpAhVFyzgGHY56BesQFjAAegQIABAB&usg=AOvVaw0iBqohES3i1-vRladIldVX",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/xkdavidkim&sa=U&ved=2ahUKEwjtqMKqktLpAhWoyDgGHewCB5IQFjAAegQIABAB&usg=AOvVaw3_HQc1ELv6xShKN-NLakct",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://www.linkedin.com/in/sandy-scott-91075433&sa=U&ved=2ahUKEwiXoM2uktLpAhW1yTgGHYR-AkoQFjAAegQIABAB&usg=AOvVaw1kDTDoqiNy79-a9l0qf01P",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/jamesbusko&sa=U&ved=2ahUKEwjGifOyktLpAhUkyDgGHSycAesQFjAAegQIABAB&usg=AOvVaw2MkDFLO_JKay_9kl3Zcb8j",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://www.linkedin.com/in/david-zhou-b6794586&sa=U&ved=2ahUKEwj7of22ktLpAhUxzTgGHdZkBGEQFjAAegQIAhAB&usg=AOvVaw1cq7IeCKfSvLh1krGVo9X5",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://www.linkedin.com/chatin/wnc/in/johncvisser%3Ftrk%3Dpeople_also_view_1&sa=U&ved=2ahUKEwiz2ZC7ktLpAhXbwzgGHQ-MDuYQFjAAegQIABAB&usg=AOvVaw1lLtoeWHIVeNz_vAuVFDkQ",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://it.linkedin.com/in/mauro-gris-7a66b65&sa=U&ved=2ahUKEwi_-8a_ktLpAhW1yzgGHcWICOAQFjAAegQIABAB&usg=AOvVaw15iNOr2rM0Sm-buqT2hpXP",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/paul-henri-rouleau-4a5a1445&sa=U&ved=2ahUKEwiVjd3DktLpAhVH4zgGHczGA-IQFjAAegQIABAB&usg=AOvVaw0xpHsGUO7MBbBXGXaTvQGV",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/sebastienpbrault&sa=U&ved=2ahUKEwi_nYbIktLpAhWsyjgGHU63BGoQFjAAegQIABAB&usg=AOvVaw1agFhLLI6Tk_JJImMqM0Bk",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/bertolo&sa=U&ved=2ahUKEwiJ4bfMktLpAhX5zTgGHdtHCRYQFjAAegQIABAB&usg=AOvVaw3iQaErHrdG6hUH7NrORCwy",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/jennifer-hardy-ogle-cpa-ca-5644772a&sa=U&ved=2ahUKEwjl-eHQktLpAhVFwzgGHevRARsQFjAAegQIABAB&usg=AOvVaw1aPr8XNx_sLuFH_WHfpceC",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/josh-malate&sa=U&ved=2ahUKEwjBp_DUktLpAhUq4jgGHaICB-8QFjAAegQIBRAB&usg=AOvVaw2zD9sxFmXNRbafjjKR12Gw",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/victor-gomez-007a182&sa=U&ved=2ahUKEwjikbLZktLpAhUe4zgGHRZ2AM4QFjAAegQIABAB&usg=AOvVaw0dP71zTNgIwXKhm_EP_VQw",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/larry-lall-a03261&sa=U&ved=2ahUKEwip9LbdktLpAhUdzzgGHbniCbUQFjAAegQIARAB&usg=AOvVaw0zI1m8i4k2KGzEHONmnfPB",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/erin-crowe-fcpa-fca-b269b1b&sa=U&ved=2ahUKEwjAyK3hktLpAhWmxDgGHUzOBeUQFjAAegQIABAB&usg=AOvVaw0kXVHUdmjmrQduTW-EX2D4",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/judith-purves-735a9b10&sa=U&ved=2ahUKEwjV6KflktLpAhV0yjgGHdjoCFMQFjAAegQIARAB&usg=AOvVaw0IMe0A3RCkmuTbPLLWIsG0",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/mihir-shah-ca-b43a1242&sa=U&ved=2ahUKEwiVr7_pktLpAhVx4zgGHQD7BHYQFjAAegQIABAB&usg=AOvVaw0QtSyyu8Fsn5UWAH6VW-Gu",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/kasia-malz&sa=U&ved=2ahUKEwj5mbftktLpAhUcxDgGHUj4AH0QFjAAegQIBhAB&usg=AOvVaw1k4i-nOm-n7KZQVIES6gfY",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://www.linkedin.com/in/jordan-parsons-02306492&sa=U&ved=2ahUKEwjZ-OfxktLpAhVUwTgGHYyTBxEQFjAAegQIABAB&usg=AOvVaw3burS4huZ4OCXagJgGmrKq",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/jonyu84&sa=U&ved=2ahUKEwiboZ72ktLpAhUnwTgGHed2DCUQFjAAegQIABAB&usg=AOvVaw2IHGCrOma39D8KzcJCaVcz",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/naelelshawwa&sa=U&ved=2ahUKEwiK9M_6ktLpAhVGzDgGHfgrDmcQFjAAegQIAhAB&usg=AOvVaw0kTslbWViFETL4K9D9NF2s",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/gvalles1&sa=U&ved=2ahUKEwi52tv-ktLpAhXNzTgGHbpTAU0QFjAAegQIABAB&usg=AOvVaw2JlRyXFbTgEEMhz99f2cfE",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/grantzobell&sa=U&ved=2ahUKEwjkk42Dk9LpAhWAyzgGHUlKB7cQFjAAegQIAxAB&usg=AOvVaw2fuZL7h5ak6oiFiwe7iuEu",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/etmalaty&sa=U&ved=2ahUKEwiLn7mHk9LpAhVpyjgGHeTJDYgQFjAAegQIARAB&usg=AOvVaw23NZcHI4KfjAA9-To7WrGc",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/noahmerriby&sa=U&ved=2ahUKEwjD-b2Lk9LpAhXhxjgGHVfnDh4QFjAAegQIAhAB&usg=AOvVaw2lYsv-2R6-I9Nnw2NqFdwu",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/ali-shahidi-msc-it-isse-b-eng-c-cissp-sep-itil-pmp-14aa25a&sa=U&ved=2ahUKEwizwuWPk9LpAhWWzjgGHS-QDM0QFjAAegQIABAB&usg=AOvVaw2xFbr7x9LrEWSf37ClqzRx",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/arslan-ansari-mba-cpa-ca-85a93615%3Ftrk%3Dpub-pbmap&sa=U&ved=2ahUKEwjIm_uTk9LpAhV_yzgGHUhdCCkQFjAAegQIARAB&usg=AOvVaw1Jzr1hI8ESnhEEjIjK3Rwk",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/shaun-martelly-b622b046&sa=U&ved=2ahUKEwjjsa2Yk9LpAhUuxjgGHcfpAg0QFjAAegQIABAB&usg=AOvVaw3utZkFRiVRRWWcUREd-YM8",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://ca.linkedin.com/in/paul-sun-66904014&sa=U&ved=2ahUKEwjgyseck9LpAhUpxTgGHTnABXgQFjAAegQIABAB&usg=AOvVaw30GbzhHdGKoi726H9d_Pc3",
            "https://www.google.com/url?client=internal-element-cse&cx=016334162055026086443:ggfqj3jdxwo&q=https://www.linkedin.com/in/chrishfoy&sa=U&ved=2ahUKEwiH2-Ogk9LpAhV7zTgGHXn8BuoQFjAAegQIABAB&usg=AOvVaw20JILoakiDIGGDkkgNEdTp"

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_main);



        if (count==0){

            Intent intent =new Intent(TestMain.this,TestNew.class);
            intent.putExtra("url",arr[count]);
            startActivity(intent);
            count++;
        }



    }
}
